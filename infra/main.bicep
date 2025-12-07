targetScope = 'subscription'

@minLength(1)
@maxLength(64)
@description('Name of the environment (e.g., dev, prod)')
param environmentName string

@minLength(1)
@description('Primary location for all resources')
param location string

// Optional: override resource names
param resourceGroupName string = ''
param containerRegistryName string = ''
param containerAppsEnvironmentName string = ''
param logAnalyticsName string = ''

// Database settings
param mysqlServerName string = ''
param mysqlDatabaseName string = 'ccdb'
param mysqlAdminUser string = 'ccuser'
@secure()
param mysqlAdminPassword string

// Backend settings
@secure()
param jwtSecret string
param jwtExpirationMillis string = '3600000'

// Gemini API
@secure()
param geminiApiKey string
param geminiApiUrl string = 'https://generativelanguage.googleapis.com/v1beta/models'
param geminiModel string = 'gemini-2.5-flash'
param geminiMaxTokens string = '80000'

// Naver API
@secure()
param naverClientId string
@secure()
param naverClientSecret string

// AWS S3
@secure()
param awsAccessKeyId string
@secure()
param awsSecretAccessKey string
param awsRegion string = 'ap-northeast-2'
param awsS3Bucket string = 'capston-cc-images'

// Container images (injected by azd)
param backendImageName string = ''
param frontendImageName string = ''

var abbrs = loadJsonContent('./abbreviations.json')
var resourceToken = toLower(uniqueString(subscription().id, environmentName, location))
var tags = { 'azd-env-name': environmentName }

// Resource Group
resource rg 'Microsoft.Resources/resourceGroups@2022-09-01' = {
  name: !empty(resourceGroupName) ? resourceGroupName : '${abbrs.resourcesResourceGroups}${environmentName}'
  location: location
  tags: tags
}

// Container Apps Environment with Log Analytics
module monitoring './core/monitor/monitoring.bicep' = {
  name: 'monitoring'
  scope: rg
  params: {
    name: !empty(logAnalyticsName) ? logAnalyticsName : '${abbrs.operationalInsightsWorkspaces}${resourceToken}'
    location: location
    tags: tags
  }
}

module containerAppsEnvironment './core/host/container-apps-environment.bicep' = {
  name: 'container-apps-environment'
  scope: rg
  params: {
    name: !empty(containerAppsEnvironmentName) ? containerAppsEnvironmentName : '${abbrs.appManagedEnvironments}${resourceToken}'
    location: location
    tags: tags
    logAnalyticsWorkspaceId: monitoring.outputs.logAnalyticsWorkspaceId
  }
}

// Container Registry
module containerRegistry './core/host/container-registry.bicep' = {
  name: 'container-registry'
  scope: rg
  params: {
    name: !empty(containerRegistryName) ? containerRegistryName : '${abbrs.containerRegistryRegistries}${resourceToken}'
    location: location
    tags: tags
  }
}

// MySQL Flexible Server
module mysql './core/database/mysql.bicep' = {
  name: 'mysql'
  scope: rg
  params: {
    name: !empty(mysqlServerName) ? mysqlServerName : '${abbrs.dBforMySQLServers}${resourceToken}'
    location: location
    tags: tags
    databaseName: mysqlDatabaseName
    administratorLogin: mysqlAdminUser
    administratorLoginPassword: mysqlAdminPassword
  }
}

// Backend Container App
module backend './app/backend.bicep' = {
  name: 'backend'
  scope: rg
  params: {
    name: '${abbrs.appContainerApps}backend-${resourceToken}'
    location: location
    tags: union(tags, { 'azd-service-name': 'backend' })
    containerAppsEnvironmentName: containerAppsEnvironment.outputs.name
    containerRegistryName: containerRegistry.outputs.name
    imageName: backendImageName
    mysqlHost: mysql.outputs.fqdn
    mysqlDatabase: mysqlDatabaseName
    mysqlUser: mysqlAdminUser
    mysqlPassword: mysqlAdminPassword
    jwtSecret: jwtSecret
    jwtExpirationMillis: jwtExpirationMillis
    geminiApiKey: geminiApiKey
    geminiApiUrl: geminiApiUrl
    geminiModel: geminiModel
    geminiMaxTokens: geminiMaxTokens
    naverClientId: naverClientId
    naverClientSecret: naverClientSecret
    awsAccessKeyId: awsAccessKeyId
    awsSecretAccessKey: awsSecretAccessKey
    awsRegion: awsRegion
    awsS3Bucket: awsS3Bucket
  }
}

// Frontend Container App
module frontend './app/frontend.bicep' = {
  name: 'frontend'
  scope: rg
  params: {
    name: '${abbrs.appContainerApps}frontend-${resourceToken}'
    location: location
    tags: union(tags, { 'azd-service-name': 'frontend' })
    containerAppsEnvironmentName: containerAppsEnvironment.outputs.name
    containerRegistryName: containerRegistry.outputs.name
    imageName: frontendImageName
    backendUrl: backend.outputs.uri
  }
}

// Outputs
output AZURE_CONTAINER_REGISTRY_ENDPOINT string = containerRegistry.outputs.loginServer
output AZURE_CONTAINER_REGISTRY_NAME string = containerRegistry.outputs.name
output AZURE_CONTAINER_APPS_ENVIRONMENT_NAME string = containerAppsEnvironment.outputs.name
output AZURE_RESOURCE_GROUP string = rg.name

output BACKEND_URI string = backend.outputs.uri
output FRONTEND_URI string = frontend.outputs.uri
output MYSQL_FQDN string = mysql.outputs.fqdn

param name string
param location string = resourceGroup().location
param tags object = {}

param containerAppsEnvironmentName string
param containerRegistryName string
param imageName string = ''

// MySQL
param mysqlHost string
param mysqlDatabase string
param mysqlUser string
@secure()
param mysqlPassword string

// JWT
@secure()
param jwtSecret string
param jwtExpirationMillis string

// Gemini
@secure()
param geminiApiKey string
param geminiApiUrl string
param geminiModel string
param geminiMaxTokens string

// Naver
@secure()
param naverClientId string
@secure()
param naverClientSecret string

// AWS
@secure()
param awsAccessKeyId string
@secure()
param awsSecretAccessKey string
param awsRegion string
param awsS3Bucket string

resource containerAppsEnvironment 'Microsoft.App/managedEnvironments@2023-05-01' existing = {
  name: containerAppsEnvironmentName
}

resource containerRegistry 'Microsoft.ContainerRegistry/registries@2023-01-01-preview' existing = {
  name: containerRegistryName
}

resource backend 'Microsoft.App/containerApps@2023-05-01' = {
  name: name
  location: location
  tags: tags
  properties: {
    managedEnvironmentId: containerAppsEnvironment.id
    configuration: {
      activeRevisionsMode: 'Single'
      ingress: {
        external: true
        targetPort: 8080
        transport: 'http'
        corsPolicy: {
          allowedOrigins: ['*']
          allowedMethods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS', 'PATCH']
          allowedHeaders: ['*']
        }
      }
      registries: [
        {
          server: containerRegistry.properties.loginServer
          username: containerRegistry.listCredentials().username
          passwordSecretRef: 'registry-password'
        }
      ]
      secrets: [
        {
          name: 'registry-password'
          value: containerRegistry.listCredentials().passwords[0].value
        }
        {
          name: 'mysql-password'
          value: mysqlPassword
        }
        {
          name: 'jwt-secret'
          value: jwtSecret
        }
        {
          name: 'gemini-api-key'
          value: geminiApiKey
        }
        {
          name: 'naver-client-id'
          value: naverClientId
        }
        {
          name: 'naver-client-secret'
          value: naverClientSecret
        }
        {
          name: 'aws-access-key-id'
          value: awsAccessKeyId
        }
        {
          name: 'aws-secret-access-key'
          value: awsSecretAccessKey
        }
      ]
    }
    template: {
      containers: [
        {
          name: 'backend'
          image: !empty(imageName) ? imageName : 'mcr.microsoft.com/azuredocs/containerapps-helloworld:latest'
          resources: {
            cpu: json('0.5')
            memory: '1Gi'
          }
          env: [
            { name: 'SPRING_PROFILES_ACTIVE', value: 'prod' }
            { name: 'MYSQL_HOST', value: mysqlHost }
            { name: 'MYSQL_PORT', value: '3306' }
            { name: 'MYSQL_DATABASE', value: mysqlDatabase }
            { name: 'MYSQL_USER', value: mysqlUser }
            { name: 'MYSQL_PASSWORD', secretRef: 'mysql-password' }
            { name: 'MYSQL_USE_SSL', value: 'true' }
            { name: 'MYSQL_REQUIRE_SSL', value: 'true' }
            { name: 'JWT_SECRET', secretRef: 'jwt-secret' }
            { name: 'JWT_EXPIRATION_MILLIS', value: jwtExpirationMillis }
            { name: 'GEMINI_API_KEY', secretRef: 'gemini-api-key' }
            { name: 'GOOGLE_API_KEY', secretRef: 'gemini-api-key' }
            { name: 'GEMINI_API_URL', value: geminiApiUrl }
            { name: 'GEMINI_MODEL', value: geminiModel }
            { name: 'GEMINI_MAX_TOKENS', value: geminiMaxTokens }
            { name: 'NAVER_CLIENT_ID', secretRef: 'naver-client-id' }
            { name: 'NAVER_CLIENT_SECRET', secretRef: 'naver-client-secret' }
            { name: 'AWS_ACCESS_KEY_ID', secretRef: 'aws-access-key-id' }
            { name: 'AWS_SECRET_ACCESS_KEY', secretRef: 'aws-secret-access-key' }
            { name: 'AWS_REGION', value: awsRegion }
            { name: 'AWS_S3_BUCKET', value: awsS3Bucket }
          ]
        }
      ]
      scale: {
        minReplicas: 0
        maxReplicas: 3
        rules: [
          {
            name: 'http-scaling'
            http: {
              metadata: {
                concurrentRequests: '50'
              }
            }
          }
        ]
      }
    }
  }
}

output id string = backend.id
output name string = backend.name
output uri string = 'https://${backend.properties.configuration.ingress.fqdn}'

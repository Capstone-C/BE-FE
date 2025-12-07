param name string
param location string = resourceGroup().location
param tags object = {}
param adminUserEnabled bool = true
param sku object = {
  name: 'Basic'
}

resource containerRegistry 'Microsoft.ContainerRegistry/registries@2023-01-01-preview' = {
  name: name
  location: location
  tags: tags
  sku: sku
  properties: {
    adminUserEnabled: adminUserEnabled
  }
}

output name string = containerRegistry.name
output loginServer string = containerRegistry.properties.loginServer
output id string = containerRegistry.id

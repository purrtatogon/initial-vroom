// Production — used by the Azure CI/CD build (ng build --configuration production).
// Points to the backend Container App's public HTTPS URL.
export const environment = {
  production: true,
  apiUrl: 'https://backend.niceriver-1bba6173.spaincentral.azurecontainerapps.io/api',
  wsUrl: 'https://backend.niceriver-1bba6173.spaincentral.azurecontainerapps.io/vroom-ws',
};

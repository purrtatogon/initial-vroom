// Production — ng build --configuration production (e.g. Render or local prod image).
// Points to the backend service public HTTPS URL.
export const environment = {
  production: true,
  apiUrl: 'https://initial-vroom-backend.onrender.com/api',
  wsUrl: 'https://initial-vroom-backend.onrender.com/vroom-ws',
};

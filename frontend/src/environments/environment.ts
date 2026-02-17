// Development — used by ng serve and Docker Compose builds.
// Points to the backend on localhost (port-mapped from the container or running natively).
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8081/api',
  wsUrl: 'http://localhost:8081/vroom-ws',
};

export const environment = {
  apiBaseUrl: 'http://localhost:9000',
  google: {
    clientId: '56250681005-gr23nhji5n2f7dpl5j46ek919oir8ai4.apps.googleusercontent.com',
    redirectUri: 'http://localhost:5173/auth/google/callback',
    scope: 'openid email profile',
    authEndpoint: 'https://accounts.google.com/o/oauth2/v2/auth'
  }
};

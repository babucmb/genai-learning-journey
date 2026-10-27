const admin = require('firebase-admin');
require('dotenv').config();

let db;

try {
  if (process.env.FIREBASE_SERVICE_ACCOUNT_KEY) {
    const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_KEY);
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
  } else if (process.env.FIREBASE_CLIENT_EMAIL && process.env.FIREBASE_PRIVATE_KEY) {
    const privateKey = process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n');
    admin.initializeApp({
      credential: admin.credential.cert({
        projectId: process.env.FIREBASE_PROJECT_ID || 'pathai-coach',
        clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
        privateKey: privateKey
      })
    });
  } else if (!admin.apps.length) {
    admin.initializeApp({
      projectId: process.env.FIREBASE_PROJECT_ID || 'pathai-coach'
    });
  }
  db = admin.firestore();
  console.log('[FirebaseAdmin] Initialized Firestore successfully');
} catch (error) {
  console.warn('[FirebaseAdmin] Warning: Running without full Admin credentials. Using fallback mock/REST adapter.', error.message);
}

module.exports = {
  admin,
  db
};

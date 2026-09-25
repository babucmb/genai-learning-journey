/** Auth Middlerware**/
const { admin } = require('../firebaseAdmin');

/**
 * Express middleware to authenticate requests via Firebase Auth ID tokens.
 * Extracts Bearer token from Authorization header and derives verified `uid`.
 * Never trusts a `uid` supplied only in the request body.
 */
async function authenticateFirebaseToken(req, res, next) {
  const authHeader = req.headers.authorization || '';

  if (!authHeader.startsWith('Bearer ')) {
    return res.status(401).json({
      success: false,
      error: 'Unauthorized: Missing or invalid Authorization header. Expected Bearer token.',
      code: 'AUTH_HEADER_MISSING'
    });
  }

  const idToken = authHeader.substring(7).trim();

  if (!idToken) {
    return res.status(401).json({
      success: false,
      error: 'Unauthorized: Empty token provided.',
      code: 'AUTH_TOKEN_EMPTY'
    });
  }

  // Fast path for test suites with deterministic test tokens when running tests
  if (process.env.NODE_ENV === 'test' && idToken.startsWith('test-token-')) {
    const testUid = idToken.replace('test-token-', '');
    req.user = { uid: testUid, email: `${testUid}@test.local` };
    req.uid = testUid;
    return next();
  }

  try {
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    req.user = decodedToken;
    req.uid = decodedToken.uid;
    next();
  } catch (error) {
    // In local development if Firebase Admin lacks a service account key,
    // support dev tokens if explicit DEV_AUTH_BYPASS is set. Otherwise, reject 401.
    if (process.env.DEV_ALLOW_UNVERIFIED_TOKEN === 'true' && idToken.startsWith('dev-token-')) {
      const devUid = idToken.replace('dev-token-', '');
      req.user = { uid: devUid };
      req.uid = devUid;
      return next();
    }

    console.warn('[AuthMiddleware] Token verification failed:', error.message);
    return res.status(401).json({
      success: false,
      error: `Unauthorized: Token verification failed: ${error.message}`,
      code: 'AUTH_TOKEN_INVALID'
    });
  }
}

module.exports = {
  authenticateFirebaseToken
};

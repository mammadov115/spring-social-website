// auth.js
import http from 'k6/http';
import { check } from 'k6';

const BASE = 'http://localhost:8080';
const headers = { 'Content-Type': 'application/json' };

export default function () {
    const email = `test_${Date.now()}@example.com`;

    // Register
    let reg = http.post(`${BASE}/api/auth/register`,
        JSON.stringify({ email, password: 'Test1234!', firstName: 'Test', lastName: 'User' }),
        { headers }
    );
    check(reg, { 'register 200': r => r.status === 200 });
    console.log(`Register: ${reg.timings.duration}ms`);

    // Login
    let login = http.post(`${BASE}/api/auth/login`,
        JSON.stringify({ email, password: 'Test1234!' }),
        { headers }
    );
    check(login, { 'login 200': r => r.status === 200 });
    console.log(`Login: ${login.timings.duration}ms`);

    // Logout
    const token = login.json('accessToken');
    let logout = http.post(`${BASE}/api/auth/logout`, null, {
        headers: { ...headers, Authorization: `Bearer ${token}` }
    });
    check(logout, { 'logout 204': r => r.status === 204 });
    console.log(`Logout: ${logout.timings.duration}ms`);
    console.log(`Logout status: ${logout.status}`);
    console.log(`Logout body: ${logout.body}`);
}
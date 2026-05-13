import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ADMIN_EMAIL = __ENV.ADMIN_EMAIL || 'admin@healthsys.local';
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || 'Admin@123';

const errorRate = new Rate('errors');
const loginDuration = new Trend('login_duration', true);
const patientCreateDuration = new Trend('patient_create_duration', true);
const patientListDuration = new Trend('patient_list_duration', true);

export const options = {
  scenarios: {
    smoke: {
      executor: 'constant-vus',
      vus: 2,
      duration: '30s',
      tags: { scenario: 'smoke' },
    },
    load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 20 },
        { duration: '3m', target: 20 },
        { duration: '1m', target: 0 },
      ],
      startTime: '35s',
      tags: { scenario: 'load' },
    },
    spike: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 50 },
        { duration: '30s', target: 50 },
        { duration: '10s', target: 0 },
      ],
      startTime: '6m',
      tags: { scenario: 'spike' },
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2000', 'p(99)<5000'],
    http_req_failed: ['rate<0.05'],
    errors: ['rate<0.05'],
    login_duration: ['p(95)<1000'],
    patient_list_duration: ['p(95)<1500'],
    patient_create_duration: ['p(95)<2000'],
  },
};

function login() {
  const payload = JSON.stringify({ email: ADMIN_EMAIL, password: ADMIN_PASSWORD });
  const params = { headers: { 'Content-Type': 'application/json' } };

  const start = Date.now();
  const res = http.post(`${BASE_URL}/api/auth/login`, payload, params);
  loginDuration.add(Date.now() - start);

  const ok = check(res, {
    'login status 200': (r) => r.status === 200,
    'login returns token': (r) => {
      try {
        return JSON.parse(r.body).token !== undefined;
      } catch {
        return false;
      }
    },
  });

  errorRate.add(!ok);
  if (!ok) return null;

  return JSON.parse(res.body).token;
}

function listPatients(token) {
  const params = { headers: { Authorization: `Bearer ${token}` } };

  const start = Date.now();
  const res = http.get(`${BASE_URL}/api/patients`, params);
  patientListDuration.add(Date.now() - start);

  const ok = check(res, {
    'list patients status 200': (r) => r.status === 200,
  });
  errorRate.add(!ok);
  return ok;
}

function createPatient(token) {
  const cpf = `${Math.floor(Math.random() * 90000000000) + 10000000000}`;
  const payload = JSON.stringify({
    name: `Paciente Teste ${Math.floor(Math.random() * 10000)}`,
    cpf: cpf,
    dateOfBirth: '1990-01-01',
    email: `paciente${cpf}@test.com`,
    phone: '11999999999',
  });
  const params = {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  };

  const start = Date.now();
  const res = http.post(`${BASE_URL}/api/patients`, payload, params);
  patientCreateDuration.add(Date.now() - start);

  const ok = check(res, {
    'create patient status 201': (r) => r.status === 201,
  });
  errorRate.add(!ok);
  return ok ? JSON.parse(res.body).id : null;
}

function getPatient(token, id) {
  const params = { headers: { Authorization: `Bearer ${token}` } };
  const res = http.get(`${BASE_URL}/api/patients/${id}`, params);
  const ok = check(res, { 'get patient status 200': (r) => r.status === 200 });
  errorRate.add(!ok);
}

export default function () {
  group('Auth Flow', () => {
    const token = login();
    if (!token) return;
    sleep(0.5);

    group('Patient CRUD', () => {
      listPatients(token);
      sleep(0.3);

      const patientId = createPatient(token);
      sleep(0.3);

      if (patientId) {
        getPatient(token, patientId);
        sleep(0.3);
      }
    });
  });

  sleep(1);
}

export function handleSummary(data) {
  return {
    'stdout': summaryReport(data),
    'scripts/load-tests/results/summary.json': JSON.stringify(data, null, 2),
  };
}

function summaryReport(data) {
  const metrics = data.metrics;
  const lines = [
    '',
    '=== HealthSys Load Test Summary ===',
    '',
    `Total requests:    ${metrics.http_reqs?.values?.count ?? 'N/A'}`,
    `Failed requests:   ${((metrics.http_req_failed?.values?.rate ?? 0) * 100).toFixed(2)}%`,
    `Error rate:        ${((metrics.errors?.values?.rate ?? 0) * 100).toFixed(2)}%`,
    '',
    `HTTP p50:          ${(metrics.http_req_duration?.values?.['p(50)'] ?? 0).toFixed(0)}ms`,
    `HTTP p95:          ${(metrics.http_req_duration?.values?.['p(95)'] ?? 0).toFixed(0)}ms`,
    `HTTP p99:          ${(metrics.http_req_duration?.values?.['p(99)'] ?? 0).toFixed(0)}ms`,
    '',
    `Login p95:         ${(metrics.login_duration?.values?.['p(95)'] ?? 0).toFixed(0)}ms`,
    `Patient list p95:  ${(metrics.patient_list_duration?.values?.['p(95)'] ?? 0).toFixed(0)}ms`,
    `Patient create p95:${(metrics.patient_create_duration?.values?.['p(95)'] ?? 0).toFixed(0)}ms`,
    '',
  ];
  return lines.join('\n');
}

import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const USER_ID = __ENV.USER_ID || "1";
const AMOUNT = Number(__ENV.AMOUNT || "1000");

export const options = {
    stages: [
        { duration: "10s", target: 10 },   // 워밍업: 10명
        { duration: "20s", target: 50 },   // 본부하: 50명
        { duration: "10s", target: 0 },    // 종료
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],        // 실패율 1% 미만
        http_req_duration: ["p(95)<2000"],     // 95%가 2초 미만 (로컬 기준 가이드)
    },
};

export default function () {
    const url = `${BASE_URL}/api/users/${USER_ID}/points/redeem`;
    const payload = JSON.stringify({
        amount: AMOUNT,
        memo: "k6-perf",
    });

    const params = {
        headers: { "Content-Type": "application/json" },
    };

    const res = http.post(url, payload, params);

    check(res, {
        "status is 200": (r) => r.status === 200,
    });

    sleep(0.1);
}
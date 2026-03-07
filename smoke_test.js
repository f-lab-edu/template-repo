import http from 'k6/http';
import { check } from 'k6';

const PORTS = [80, 81];
const USER_ID = 'test-user';

export let options = {
    scenarios: {
        chat_messages: {
            executor: 'constant-arrival-rate',
            rate: 2,
            timeUnit: '1s',
            duration: '120s',
            preAllocatedVUs: 5,
            maxVUs: 20,
        },
    },
};

// 포트를 랜덤하게 선택해서 base URL 생성
function getRandomBaseUrl() {
    const port = PORTS[Math.floor(Math.random() * PORTS.length)];
    return `http://localhost:${port}/api`;
    // return `http://localhost:80/api`;
}

// setup()에서 채팅 생성 + 참가
export function setup() {
    const BASE_URL = getRandomBaseUrl();

    // 1. 채팅 생성
    let createPayload = JSON.stringify({
        userId: USER_ID,
        title: 'k6 Load Test Chat'
    });

    let createRes = http.post(`${BASE_URL}/chats`, createPayload, {
        headers: { 'Content-Type': 'application/json' },
    });

    check(createRes, { 'chat created': (r) => r.status === 201 });

    const chatId = createRes.json()['chatId'];

    // 2. 채팅 참가
    let joinPayload = JSON.stringify({ userId: USER_ID });

    let joinRes = http.post(`${BASE_URL}/chats/${chatId}/participants`, joinPayload, {
        headers: { 'Content-Type': 'application/json' },
    });

    check(joinRes, { 'joined chat': (r) => r.status === 200 });

    return { chatId };
}

// default function: 메시지 전송
export default function (data) {
    const BASE_URL = getRandomBaseUrl();
    const chatId = data.chatId;

    const headers = {
        'Content-Type': 'application/json',
        'user-id': USER_ID
    };
    const payload = JSON.stringify({
        userId: USER_ID,
        content: 'Hello from k6'
    });

    const res = http.post(`${BASE_URL}/chats/${chatId}/messages`, payload, 
      { headers },
    );

    check(res, { 'message sent': (r) => r.status === 200 });
}
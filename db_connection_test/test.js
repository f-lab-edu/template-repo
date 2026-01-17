import http from 'k6/http';

export function setup() {
  const url = 'http://localhost:8080/api/chats';
  
  const payload = JSON.stringify({
    userId: "test user",
    title: "Test chat"
  });
  
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };
  
  const loginResponse = http.post(url, payload, params);
  
  const chatId = loginResponse.json('chatId');
  return { chatId };
}

export default function (data) {
  const url = 'http://localhost:8080/api/chats/' + data.chatId + '/messages';
  
  const payload = JSON.stringify({
    userId: 'test user',
    content: 'test message',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(url, payload, params);
}

export const options = {
  scenarios: {
    rps_test: {
      executor: 'constant-arrival-rate',
      rate: 1000,
      timeUnit: '1s',
      duration: '60s',
      preAllocatedVUs: 1400
    }
  }
};
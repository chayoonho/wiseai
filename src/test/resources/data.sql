-- 사용자 (3명)
INSERT INTO users (name, email)
VALUES ('User2', 'user1@example.com');
INSERT INTO users (name, email)
VALUES ('User3', 'user2@example.com');
INSERT INTO users (name, email)
VALUES ('User4', 'user3@example.com');

-- 회의실 (id=1)
INSERT INTO meeting_rooms (id, name, capacity, hourly_rate)
VALUES (1, '테스트회의실', 10, 10000);

-- 결제 제공자 (3종)
INSERT INTO payment_providers (name, api_endpoint, auth_info)
VALUES ('Card', 'https://mock-api.local/card/pay', 'API_KEY_CARD_123');
INSERT INTO payment_providers (name, api_endpoint, auth_info)
VALUES ('Simple', 'https://mock-api.local/simple/pay', 'API_KEY_SIMPLE_123');
INSERT INTO payment_providers (name, api_endpoint, auth_info)
VALUES ('VirtualAccount', 'https://mock-api.local/virtual/pay', 'API_KEY_VIRTUAL_123');

-- 예약 데이터 (동시성 테스트용, 3명 각각 1건씩)
-- User1 예약
INSERT INTO reservations (meeting_room_id, start_time, end_time, status, total_amount, user_id, version)
VALUES (1, '2025-09-02 10:00:00', '2025-09-02 11:00:00', 'PENDING_PAYMENT', 10000, 1, 0);

-- User2 예약
INSERT INTO reservations (meeting_room_id, start_time, end_time, status, total_amount, user_id, version)
VALUES (1, '2025-09-02 11:00:00', '2025-09-02 12:00:00', 'PENDING_PAYMENT', 10000, 2, 0);

-- User3 예약
INSERT INTO reservations (meeting_room_id, start_time, end_time, status, total_amount, user_id, version)
VALUES (1, '2025-09-02 13:00:00', '2025-09-02 14:00:00', 'PENDING_PAYMENT', 10000, 3, 0);

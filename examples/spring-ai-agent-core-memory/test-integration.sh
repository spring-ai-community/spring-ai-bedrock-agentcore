#!/bin/bash

set -e

BASE_URL="http://localhost:8080"
CONVERSATION_ID="test-$(date +%s)"

echo "🧪 Running Integration Tests for Spring AI AgentCore Memory Example"
echo "Using conversation ID: $CONVERSATION_ID"

# Test 1: Send first message
echo "📤 Test 1: Sending first message..."
RESPONSE1=$(curl -s -X POST "$BASE_URL/api/chat/$CONVERSATION_ID" \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello, my name is Alice and I love hiking"}')

echo "Response: $RESPONSE1"
if [[ $RESPONSE1 == *"Alice"* ]]; then
  echo "✅ Test 1 passed: AI recognized the name"
else
  echo "❌ Test 1 failed: AI did not recognize the name"
  exit 1
fi

# Test 2: Test memory recall
echo "📤 Test 2: Testing memory recall..."
RESPONSE2=$(curl -s -X POST "$BASE_URL/api/chat/$CONVERSATION_ID" \
  -H "Content-Type: application/json" \
  -d '{"message": "What do you remember about me?"}')

echo "Response: $RESPONSE2"
if [[ $RESPONSE2 == *"Alice"* ]] && [[ $RESPONSE2 == *"hiking"* ]]; then
  echo "✅ Test 2 passed: AI remembered previous context"
else
  echo "❌ Test 2 failed: AI did not remember context"
  exit 1
fi

# Test 3: Get conversation history
echo "📤 Test 3: Getting conversation history..."
HISTORY=$(curl -s "$BASE_URL/api/chat/$CONVERSATION_ID/history")

echo "History: $HISTORY"
if [[ $HISTORY == *"Alice"* ]] && [[ $HISTORY == *"hiking"* ]]; then
  echo "✅ Test 3 passed: History contains expected content"
else
  echo "❌ Test 3 failed: History missing expected content"
  exit 1
fi

# Test 4: Test separate conversation
echo "📤 Test 4: Testing conversation isolation..."
OTHER_CONVERSATION="other-test-$(date +%s)"
RESPONSE3=$(curl -s -X POST "$BASE_URL/api/chat/$OTHER_CONVERSATION" \
  -H "Content-Type: application/json" \
  -d '{"message": "Do you know anything about Alice?"}')

echo "Response: $RESPONSE3"
if [[ $RESPONSE3 != *"hiking"* ]]; then
  echo "✅ Test 4 passed: Conversations are properly isolated"
else
  echo "❌ Test 4 failed: Conversation isolation not working"
  exit 1
fi

# Test 5: Clear conversation
echo "📤 Test 5: Clearing conversation..."
curl -s -X DELETE "$BASE_URL/api/chat/$CONVERSATION_ID"

# Verify conversation is cleared
CLEARED_HISTORY=$(curl -s "$BASE_URL/api/chat/$CONVERSATION_ID/history")
if [[ $CLEARED_HISTORY == "[]" ]]; then
  echo "✅ Test 5 passed: Conversation cleared successfully"
else
  echo "❌ Test 5 failed: Conversation not cleared"
  exit 1
fi

# Cleanup
curl -s -X DELETE "$BASE_URL/api/chat/$OTHER_CONVERSATION"

echo "🎉 All integration tests passed!"
echo "✅ Memory persistence working"
echo "✅ Context awareness working"
echo "✅ Conversation isolation working"
echo "✅ History management working"

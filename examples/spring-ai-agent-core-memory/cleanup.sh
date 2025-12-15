#!/bin/bash

# Cleanup script for AgentCore Memory

MEMORY_ID=${1:-${AGENTCORE_MEMORY_ID}}

if [ -z "$MEMORY_ID" ]; then
    echo "Usage: $0 <memory-id>"
    echo "Or set AGENTCORE_MEMORY_ID environment variable"
    exit 1
fi

echo "Deleting AgentCore Memory: $MEMORY_ID"
aws bedrock-agentcore-control delete-memory --memory-id "$MEMORY_ID" --region us-east-1

if [ $? -eq 0 ]; then
    echo "Memory $MEMORY_ID deleted successfully"
else
    echo "Failed to delete memory $MEMORY_ID"
    exit 1
fi

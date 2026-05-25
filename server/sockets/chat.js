module.exports = function(io, socket, rooms) {
  
  // Realtime instant chat message receipt
  socket.on('send_message', (data) => {
    const { roomId, senderId, senderName, senderAvatar, message, messageType } = data;
    if (!roomId || !message) return;

    const room = rooms[roomId];
    if (!room) return;

    const fullMessage = {
      messageId: `msg_${Date.now()}_${Math.random().toString(36).substr(2, 5)}`,
      roomId,
      senderId,
      senderName,
      senderAvatar: senderAvatar || "🎵",
      message: message.trim(),
      createdAt: Date.now(),
      messageType: messageType || "USER"
    };

    // Save history locally for room persistence
    room.chatHistory.push(fullMessage);
    if (room.chatHistory.length > 200) {
      room.chatHistory.shift(); // Bound memory capacity
    }

    // Broadcast messages instantly in real-time to all matching peers
    io.to(roomId).emit('new_message', fullMessage);
    console.log(`[Chat] Msg in ${roomId} by ${senderName}: "${message}"`);
  });
};

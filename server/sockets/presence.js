module.exports = function(io, socket, rooms) {
  
  // Client joins a Room
  socket.on('join_room', (data) => {
    const { roomId, userId, userName, userAvatar } = data;
    if (!roomId || !userId) return;

    socket.join(roomId);

    // Initialize room if it doesn't already exist in memory
    if (!rooms[roomId]) {
      rooms[roomId] = {
        id: roomId,
        name: `${userName}'s Listening Space`,
        description: "Shared online real-time listening room.",
        hostId: userId,
        hostUsername: userName,
        isPlaying: false,
        currentSongId: "dQw4w9WgXcQ", // Default video
        currentSongTitle: "Never Gonna Give You Up",
        currentSongArtist: "Rick Astley",
        currentSongDuration: 212000,
        currentPlaybackPosition: 0,
        isLocked: false,
        lastUpdated: Date.now(),
        queue: [],
        users: [],
        chatHistory: []
      };
    }

    const room = rooms[roomId];

    // Deduplicate user connection
    const existingIndex = room.users.findIndex(u => u.userId === userId);
    if (existingIndex !== -1) {
      room.users[existingIndex].socketId = socket.id;
    } else {
      room.users.push({
        socketId: socket.id,
        userId,
        userName,
        userAvatar
      });
    }

    console.log(`[Presence] User ${userName} joined room: ${roomId}. Total members: ${room.users.length}`);

    // Confirm connection back to the sender
    socket.emit('room_joined', {
      roomId,
      roomState: {
        id: room.id,
        name: room.name,
        description: room.description,
        hostId: room.hostId,
        hostUsername: room.hostUsername,
        isPlaying: room.isPlaying,
        currentSongId: room.currentSongId,
        currentSongTitle: room.currentSongTitle,
        currentSongArtist: room.currentSongArtist,
        currentSongDuration: room.currentSongDuration,
        currentPlaybackPosition: room.currentPlaybackPosition,
        isLocked: room.isLocked,
        lastUpdated: room.lastUpdated,
      },
      queue: room.queue,
      chatHistory: room.chatHistory.slice(-50), // Send last 50 messages
      users: room.users
    });

    // Notify other peers in the room
    socket.to(roomId).emit('user_joined', {
      userId,
      userName,
      userAvatar,
      memberCount: room.users.length
    });

    // Broadcast automated system notification inside room
    const sysMsg = {
      messageId: `sys_${Date.now()}_join`,
      roomId,
      senderId: "SYSTEM",
      senderName: "System",
      senderAvatar: "⚡",
      content: `${userName} connected to this synchronizing frequency.`,
      createdAt: Date.now(),
      messageType: "SYSTEM"
    };
    room.chatHistory.push(sysMsg);
    io.to(roomId).emit('new_message', sysMsg);
  });

  // Client manually leaves a Room
  socket.on('leave_room', (data) => {
    const { roomId, userId, userName } = data;
    if (!roomId) return;

    socket.leave(roomId);

    const room = rooms[roomId];
    if (room) {
      room.users = room.users.filter(u => u.userId !== userId);
      console.log(`[Presence] User ${userName} left room manually: ${roomId}. Total members remaining: ${room.users.length}`);

      // Broadcast presence state
      io.to(roomId).emit('user_left', {
        userId,
        userName,
        memberCount: room.users.length
      });

      const sysMsg = {
        messageId: `sys_${Date.now()}_leave`,
        roomId,
        senderId: "SYSTEM",
        senderName: "System",
        senderAvatar: "🚪",
        content: `${userName} left the tunnel.`,
        createdAt: Date.now(),
        messageType: "SYSTEM"
      };
      room.chatHistory.push(sysMsg);
      io.to(roomId).emit('new_message', sysMsg);
    }
  });

  // Handle live typing states
  socket.on('typing_start', (data) => {
    const { roomId, userName } = data;
    if (!roomId) return;
    socket.to(roomId).emit('typing_update', {
      isTyping: true,
      userName: userName
    });
  });

  socket.on('typing_stop', (data) => {
    const { roomId } = data;
    if (!roomId) return;
    socket.to(roomId).emit('typing_update', {
      isTyping: false,
      userName: ""
    });
  });

  // Instant emoji reaction blast
  socket.on('reaction_sent', (data) => {
    const { roomId, emoji, userName } = data;
    if (!roomId) return;
    io.to(roomId).emit('room_reactions', {
      emoji,
      userName
    });
  });
};

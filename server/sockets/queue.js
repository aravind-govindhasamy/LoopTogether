module.exports = function(io, socket, rooms) {

  // Collaborative queue: Client appends or edits a track
  socket.on('queue_video', (data) => {
    const { roomId, videoId, title, artist, duration, addedByUserId, addedByUsername } = data;
    const room = rooms[roomId];
    if (!room) return;

    const queueItem = {
      id: `q_${Date.now()}_${Math.random().toString(36).substr(2, 4)}`,
      videoId,
      title,
      artist: artist || "Unknown Artist",
      duration: duration || 180000,
      addedByUserId,
      addedByUsername,
      voteCount: 0,
      position: room.queue.length
    };

    room.queue.push(queueItem);

    // Broadcast queue update instantly inside listening room
    io.to(roomId).emit('queue_updated', {
      queue: room.queue
    });

    console.log(`[Queue] Track added in Room ${roomId}: "${title}"`);
  });

  // Skipped next song or automated track succession
  socket.on('next_video', (data) => {
    const { roomId, userId } = data;
    const room = rooms[roomId];
    if (!room) return;

    if (room.queue.length > 0) {
      // Dequeue next track in array
      const nextTrack = room.queue.shift();

      // Recalculate remaining queue positions
      room.queue.forEach((item, idx) => {
        item.position = idx;
      });

      // Update room playback state
      room.currentSongId = nextTrack.videoId;
      room.currentSongTitle = nextTrack.title;
      room.currentSongArtist = nextTrack.artist;
      room.currentSongDuration = nextTrack.duration;
      room.currentPlaybackPosition = 0;
      room.isPlaying = true;
      room.lastUpdated = Date.now();

      // Broadcast room level metadata change 
      io.to(roomId).emit('sync_state', {
        currentSongId: room.currentSongId,
        currentSongTitle: room.currentSongTitle,
        currentSongArtist: room.currentSongArtist,
        currentSongDuration: room.currentSongDuration,
        currentPlaybackPosition: room.currentPlaybackPosition,
        isPlaying: room.isPlaying,
        lastUpdated: room.lastUpdated
      });

      // Broadcast queue update also
      io.to(roomId).emit('queue_updated', {
        queue: room.queue
      });

      const sysMsg = {
        messageId: `sys_${Date.now()}_next`,
        roomId,
        senderId: "SYSTEM",
        senderName: "System",
        senderAvatar: "⏭️",
        content: `Now syncing stream: '${nextTrack.title}' added by ${nextTrack.addedByUsername}.`,
        createdAt: Date.now(),
        messageType: "SYSTEM"
      };
      room.chatHistory.push(sysMsg);
      io.to(roomId).emit('new_message', sysMsg);

      console.log(`[Queue] Track succeeded to next track in room ${roomId}: "${nextTrack.title}"`);
    } else {
      // Stream concluded
      room.isPlaying = false;
      room.currentPlaybackPosition = 0;
      room.lastUpdated = Date.now();

      io.to(roomId).emit('sync_state', {
        currentSongId: room.currentSongId,
        currentSongTitle: room.currentSongTitle,
        currentSongArtist: room.currentSongArtist,
        currentSongDuration: room.currentSongDuration,
        currentPlaybackPosition: 0,
        isPlaying: room.isPlaying,
        lastUpdated: room.lastUpdated
      });

      const sysMsg = {
        messageId: `sys_${Date.now()}_end`,
        roomId,
        senderId: "SYSTEM",
        senderName: "System",
        senderAvatar: "💿",
        content: `Queue has empty state. Add some tracks now!`,
        createdAt: Date.now(),
        messageType: "SYSTEM"
      };
      room.chatHistory.push(sysMsg);
      io.to(roomId).emit('new_message', sysMsg);
    }
  });

  // Upvote item track inside queue
  socket.on('vote_video', (data) => {
    const { roomId, itemId } = data;
    const room = rooms[roomId];
    if (!room) return;

    const index = room.queue.findIndex(item => item.id === itemId);
    if (index !== -1) {
      room.queue[index].voteCount += 1;

      // Maintain order sorted descending by votes
      room.queue.sort((a, b) => b.voteCount - a.voteCount);

      // Re-assign positions
      room.queue.forEach((item, idx) => {
        item.position = idx;
      });

      io.to(roomId).emit('queue_updated', {
        queue: room.queue
      });

      console.log(`[Queue] Vote updated for item ${itemId} in room ${roomId}`);
    }
  });
};

module.exports = function(io, socket, rooms) {

  // Host or authorized peer commands video play state
  socket.on('play_video', (data) => {
    const { roomId, userId, positionMs } = data;
    const room = rooms[roomId];
    if (!room) return;

    room.isPlaying = true;
    if (positionMs !== undefined) {
      room.currentPlaybackPosition = positionMs;
    }
    room.lastUpdated = Date.now();

    // Broadcast playback update to everyone else in the room
    io.to(roomId).emit('playback_started', {
      isPlaying: true,
      currentPlaybackPosition: room.currentPlaybackPosition,
      lastUpdated: room.lastUpdated,
      triggeredBy: userId
    });

    console.log(`[Playback] Video started in Room ${roomId} at position ${room.currentPlaybackPosition}ms`);
  });

  // Pause video state
  socket.on('pause_video', (data) => {
    const { roomId, userId, positionMs } = data;
    const room = rooms[roomId];
    if (!room) return;

    room.isPlaying = false;
    if (positionMs !== undefined) {
      room.currentPlaybackPosition = positionMs;
    }
    room.lastUpdated = Date.now();

    io.to(roomId).emit('playback_paused', {
      isPlaying: false,
      currentPlaybackPosition: room.currentPlaybackPosition,
      lastUpdated: room.lastUpdated,
      triggeredBy: userId
    });

    console.log(`[Playback] Video paused in Room ${roomId} at position ${room.currentPlaybackPosition}ms`);
  });

  // Absolute seek position inside listening tunnel
  socket.on('seek_video', (data) => {
    const { roomId, userId, positionMs } = data;
    const room = rooms[roomId];
    if (!room) return;

    room.currentPlaybackPosition = positionMs;
    room.lastUpdated = Date.now();

    io.to(roomId).emit('playback_seeked', {
      isPlaying: room.isPlaying,
      currentPlaybackPosition: room.currentPlaybackPosition,
      lastUpdated: room.lastUpdated,
      triggeredBy: userId
    });

    console.log(`[Playback] Video seeked in Room ${roomId} to ${room.currentPlaybackPosition}ms`);
  });

  // Full synchronize tick state comparison
  socket.on('sync_request', (data) => {
    const { roomId, userId, localDuration, localIsPlaying } = data;
    const room = rooms[roomId];
    if (!room) return;

    // Calculate current running server position based on updated delta
    let currentPos = room.currentPlaybackPosition;
    if (room.isPlaying && room.lastUpdated) {
      const elapsed = Date.now() - room.lastUpdated;
      currentPos += elapsed;
      if (currentPos > room.currentSongDuration) {
        currentPos = room.currentSongDuration;
      }
    }

    // Emit sync feedback directly to the requester
    socket.emit('sync_state', {
      currentSongId: room.currentSongId,
      currentSongTitle: room.currentSongTitle,
      currentSongArtist: room.currentSongArtist,
      currentSongDuration: room.currentSongDuration,
      currentPlaybackPosition: currentPos,
      isPlaying: room.isPlaying,
      lastUpdated: room.lastUpdated
    });
  });
};

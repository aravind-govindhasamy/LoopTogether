const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');
const fs = require('fs');
const path = require('path');

// Proactively load .env from root to safely capture and lock YOUTUBE_API_KEY / secrets
try {
  const envPath = path.join(__dirname, '../.env');
  if (fs.existsSync(envPath)) {
    const envContent = fs.readFileSync(envPath, 'utf8');
    envContent.split('\n').forEach(line => {
      const parts = line.split('=');
      if (parts.length >= 2) {
        const key = parts[0].trim();
        const value = parts.slice(1).join('=').trim().replace(/(^"|"$)/g, '');
        if (key && !key.startsWith('#')) {
          process.env[key] = value;
        }
      }
    });
    console.log("[Env] Root .env parsed successfully");
  } else {
    console.log("[Env] No root .env file found. Standard environment definitions apply.");
  }
} catch (e) {
  console.log("[Env] Root env parsing error ignored:", e.message);
}

// Initialize Express and App server
const app = express();
app.use(cors());
app.use(express.json());

const server = http.createServer(app);

// Initialize Socket.io Server
const io = new Server(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"]
  }
});

// Centralized In-Memory Database for Room Data & Persistence (Ready to bridge to MongoDB/Firestore)
const rooms = {}; 
// Structure of rooms[roomId]:
// {
//   id: "SYNTH-88",
//   name: "Synthwave Sunset Glow",
//   description: "Outrun beats...",
//   hostId: "host_user_id",
//   hostUsername: "HostName",
//   isPlaying: false,
//   currentSongId: "dQw4w9WgXcQ",
//   currentSongTitle: "Never Gonna Give You Up",
//   currentSongArtist: "Rick Astley",
//   currentSongDuration: 212000,
//   currentPlaybackPosition: 0,
//   isLocked: false,
//   lastUpdated: Date.now(),
//   queue: [],
//   users: [], // { socketId, userId, userName, userAvatar }
//   chatHistory: [] // { messageId, senderId, senderName, senderAvatar, content, timestamp, isSystem, messageType }
// }

// Simple REST endpoints for initial diagnostic dashboard
app.get('/api/status', (req, res) => {
  res.json({
    status: "healthy",
    uptime: process.uptime(),
    activeRooms: Object.keys(rooms).length,
    timestamp: Date.now()
  });
});

app.get('/api/rooms', (req, res) => {
  res.json(Object.values(rooms).map(r => ({
    id: r.id,
    name: r.name,
    memberCount: r.users.length,
    currentSongTitle: r.currentSongTitle,
    isPlaying: r.isPlaying
  })));
});

// Real YouTube Search API Secure Proxy
app.get('/api/search', async (req, res) => {
  const query = req.query.q || '';
  const filter = req.query.filter || 'video';
  
  const apiKey = process.env.YOUTUBE_API_KEY;
  if (!apiKey || apiKey === 'YOUR_YOUTUBE_API_KEY' || apiKey.trim() === '') {
    console.warn("[YouTube Proxy] Key empty/missing from .env!");
    return res.status(403).json({
      error: "YOUTUBE_API_KEY_MISSING",
      message: "YouTube Data API Key is not configured on your backend server. Please enter your YOUTUBE_API_KEY in the Secrets panel."
    });
  }

  try {
    // 1) Query Google YouTube search.list
    const searchUrl = `https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=15&q=${encodeURIComponent(query)}&type=video&key=${apiKey}`;
    const searchRes = await fetch(searchUrl);
    if (!searchRes.ok) {
      const searchErr = await searchRes.json().catch(() => ({}));
      console.error("[YouTube Proxy] Upstream error:", searchErr);
      return res.status(searchRes.status).json({
        error: "YOUTUBE_SEARCH_FAILED",
        details: searchErr
      });
    }

    const searchData = await searchRes.json();
    const items = searchData.items || [];
    if (items.length === 0) {
      return res.json([]);
    }

    const videoIds = items.map(item => item.id.videoId).filter(id => id);
    if (videoIds.length === 0) {
      return res.json([]);
    }

    // 2) Query Google YouTube videos.list for detailed durations and view counts
    const videoDetailsUrl = `https://www.googleapis.com/youtube/v3/videos?part=contentDetails,statistics&id=${videoIds.join(',')}&key=${apiKey}`;
    const detailsRes = await fetch(videoDetailsUrl);
    let detailsMap = {};
    if (detailsRes.ok) {
      const detailsData = await detailsRes.json();
      (detailsData.items || []).forEach(v => {
        detailsMap[v.id] = {
          duration: parseISO8601Duration(v.contentDetails.duration),
          viewCount: formatViewCount(v.statistics.viewCount)
        };
      });
    }

    // 3) Map to beautiful SongSearchModels for the Jetpack Compose frontend
    const results = items.map(item => {
      const vId = item.id.videoId;
      const details = detailsMap[vId] || { duration: 180000, viewCount: "10K views" };
      return {
        videoId: vId,
        title: item.snippet.title,
        artist: item.snippet.channelTitle,
        durationMs: details.duration,
        coverUrl: item.snippet.thumbnails.high ? item.snippet.thumbnails.high.url : (item.snippet.thumbnails.medium ? item.snippet.thumbnails.medium.url : ""),
        publishDate: sanitizePublishDate(item.snippet.publishedAt),
        viewCount: details.viewCount
      };
    });

    res.json(results);
  } catch (err) {
    console.error("[YouTube Proxy] Fetch error:", err);
    res.status(500).json({ error: "INTERNAL_SERVER_ERROR", message: err.message });
  }
});

// Helper functions for ISO 8601 parsing and metric formatting
function parseISO8601Duration(durationString) {
  const regex = /PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/;
  const matches = durationString.match(regex);
  if (!matches) return 180000;
  const hours = parseInt(matches[1] || 0);
  const minutes = parseInt(matches[2] || 0);
  const seconds = parseInt(matches[3] || 0);
  return ((hours * 3600) + (minutes * 60) + seconds) * 1000;
}

function formatViewCount(viewCountStr) {
  if (!viewCountStr) return "N/A";
  const num = parseInt(viewCountStr);
  if (num >= 1000000000) return (num / 1000000000).toFixed(1) + "B views";
  if (num >= 1000000) return (num / 1000000).toFixed(1) + "M views";
  if (num >= 1000) return (num / 1000).toFixed(1) + "K views";
  return num + " views";
}

function sanitizePublishDate(publishDateStr) {
  if (!publishDateStr) return "";
  try {
    const d = new Date(publishDateStr);
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  } catch (e) {
    return publishDateStr.split('T')[0];
  }
}

// Socket.io Modular Handlers
const handlePresence = require('./sockets/presence');
const handleChat = require('./sockets/chat');
const handlePlayback = require('./sockets/playback');
const handleQueue = require('./sockets/queue');

io.on('connection', (socket) => {
  console.log(`[Socket] Peer connected: ${socket.id}`);

  // Setup presence events
  handlePresence(io, socket, rooms);

  // Setup chat message actions
  handleChat(io, socket, rooms);

  // Setup playback authority and sync engine
  handlePlayback(io, socket, rooms);

  // Setup collaborative queue management
  handleQueue(io, socket, rooms);

  socket.on('disconnect', () => {
    console.log(`[Socket] Peer disconnected: ${socket.id}`);
    
    // Auto-clean disconnected users across all active rooms
    Object.keys(rooms).forEach(roomId => {
      const room = rooms[roomId];
      const userIndex = room.users.findIndex(u => u.socketId === socket.id);
      if (userIndex !== -1) {
        const leavingUser = room.users[userIndex];
        room.users.splice(userIndex, 1);
        console.log(`[Socket] Cleaned ${leavingUser.userName} from room ${roomId}`);

        // Broadcast presence update
        io.to(roomId).emit('user_left', {
          userId: leavingUser.userId,
          userName: leavingUser.userName,
          memberCount: room.users.length
        });

        // System message logging
        const sysMsg = {
          messageId: `sys_${Date.now()}_${Math.random().toString(36).substr(2, 4)}`,
          roomId: roomId,
          senderId: "SYSTEM",
          senderName: "System",
          senderAvatar: "🚪",
          content: `${leavingUser.userName} disconnected from the listening stream.`,
          createdAt: Date.now(),
          messageType: "SYSTEM"
        };
        room.chatHistory.push(sysMsg);
        io.to(roomId).emit('new_message', sysMsg);
      }
    });
  });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`⚡ LoopTogether backend server listening on port ${PORT}`);
});

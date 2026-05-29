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

// Curated list of high-quality playable YouTube streams/videos for robust fallback searches
const CURATED_FALLBACKS = [
  {
    videoId: "dQw4w9WgXcQ",
    title: "Rick Astley - Never Gonna Give You Up (Official Music Video)",
    artist: "Rick Astley",
    durationMs: 212000,
    coverUrl: "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
    publishDate: "Oct 25, 2009",
    viewCount: "1.4B views"
  },
  {
    videoId: "4xDzrJKXOOY",
    title: "SYNTHWAVE Mix - Lofi Retro Beats for Late Night Driving",
    artist: "Lofi Records",
    durationMs: 3600000,
    coverUrl: "https://img.youtube.com/vi/4xDzrJKXOOY/hqdefault.jpg",
    publishDate: "Jan 12, 2023",
    viewCount: "5.8M views"
  },
  {
    videoId: "5qap5aO4i9A",
    title: "Lofi Hip Hop Radio 📚 Beats to Relax/Study to",
    artist: "Lofi Girl",
    durationMs: 14400000,
    coverUrl: "https://img.youtube.com/vi/5qap5aO4i9A/hqdefault.jpg",
    publishDate: "Live Stream",
    viewCount: "38K watching"
  },
  {
    videoId: "jfKfPfyJRdk",
    title: "lofi hip hop radio ☕ beats to relax/study to",
    artist: "Lofi Girl",
    durationMs: 7200000,
    coverUrl: "https://img.youtube.com/vi/jfKfPfyJRdk/hqdefault.jpg",
    publishDate: "Mar 12, 2022",
    viewCount: "668M views"
  },
  {
    videoId: "fA7o222EozI",
    title: "The Midnight - Sunset (Official Music Video)",
    artist: "The Midnight",
    durationMs: 254000,
    coverUrl: "https://img.youtube.com/vi/fA7o222EozI/hqdefault.jpg",
    publishDate: "Oct 13, 2016",
    viewCount: "21M views"
  },
  {
    videoId: "mvGAt3KByrU",
    title: "FM-84 - Running in the Night (feat. Ollie Wride)",
    artist: "FM-84",
    durationMs: 270000,
    coverUrl: "https://img.youtube.com/vi/mvGAt3KByrU/hqdefault.jpg",
    publishDate: "Jun 14, 2016",
    viewCount: "13M views"
  },
  {
    videoId: "U8g80f8D9jA",
    title: "synthwave radio 🌌 beats to drive/game/relax to",
    artist: "Lofi Girl",
    durationMs: 7200000,
    coverUrl: "https://img.youtube.com/vi/U8g80f8D9jA/hqdefault.jpg",
    publishDate: "Nov 2, 2023",
    viewCount: "4.2M views"
  },
  {
    videoId: "tNtS91T-U_M",
    title: "Kavinsky - Nightcall (Official Video)",
    artist: "Kavinsky",
    durationMs: 258000,
    coverUrl: "https://img.youtube.com/vi/tNtS91T-U_M/hqdefault.jpg",
    publishDate: "Jan 1, 2012",
    viewCount: "250M views"
  },
  {
    videoId: "9_eXG7qKzCc",
    title: "Gunship - Tech Noir (Official Music Video)",
    artist: "Gunship",
    durationMs: 301000,
    coverUrl: "https://img.youtube.com/vi/9_eXG7qKzCc/hqdefault.jpg",
    publishDate: "May 1, 2015",
    viewCount: "18M views"
  },
  {
    videoId: "4A9zR5l9K8E",
    title: "HOME - Resonance",
    artist: "HOME / Electronic",
    durationMs: 211000,
    coverUrl: "https://img.youtube.com/vi/4A9zR5l9K8E/hqdefault.jpg",
    publishDate: "Aug 12, 2014",
    viewCount: "105M views"
  },
  {
    videoId: "gM7Hlg75Mlo",
    title: "HOME - Intro (Odyssey Album)",
    artist: "HOME",
    durationMs: 202000,
    coverUrl: "https://img.youtube.com/vi/gM7Hlg75Mlo/hqdefault.jpg",
    publishDate: "Jul 1, 2014",
    viewCount: "1.2M views"
  },
  {
    videoId: "0kL_f8Ssh2s",
    title: "Sunset coast retrowave drive - 1 Hour Extended Loop",
    artist: "Retro Dreams",
    durationMs: 3600000,
    coverUrl: "https://img.youtube.com/vi/0kL_f8Ssh2s/hqdefault.jpg",
    publishDate: "Nov 3, 2022",
    viewCount: "2.4M views"
  }
];

const https = require('https');

function fetchJSON(url) {
  return new Promise((resolve, reject) => {
    https.get(url, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        try {
          if (res.statusCode < 200 || res.statusCode >= 300) {
            reject(new Error(`HTTP Error ${res.statusCode}: ${data || 'Empty Response'}`));
          } else {
            resolve(JSON.parse(data));
          }
        } catch (e) {
          reject(e);
        }
      });
    }).on('error', (err) => {
      reject(err);
    });
  });
}

function fetchYouTubeHTML(query) {
  return new Promise((resolve, reject) => {
    const url = `https://www.youtube.com/results?search_query=${encodeURIComponent(query)}&sp=EgIQAQ%253D%253D`;
    
    const options = {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36',
        'Accept-Language': 'en-US,en;q=0.9',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8'
      },
      timeout: 5000
    };

    https.get(url, options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => { resolve(data); });
    }).on('error', (err) => {
      reject(err);
    });
  });
}

function parseSimpleDuration(str) {
  if (!str) return 180000;
  const parts = str.split(':').map(Number);
  let secs = 0;
  if (parts.length === 3) {
    secs = parts[0] * 3600 + parts[1] * 60 + parts[2];
  } else if (parts.length === 2) {
    secs = parts[0] * 60 + parts[1];
  } else {
    secs = parts[0] || 180;
  }
  return secs * 1000;
}

async function scrapeYouTubeSearch(query) {
  try {
    const html = await fetchYouTubeHTML(query);
    const regex = /ytInitialData\s*=\s*({.+?});/;
    const match = html.match(regex);
    if (!match) {
      console.warn("[Scraper] No ytInitialData object matched in html");
      return null;
    }
    
    const data = JSON.parse(match[1]);
    const contents = data.contents?.twoColumnSearchResultRenderer?.primaryContents?.sectionListRenderer?.contents?.[0]?.itemSectionRenderer?.contents || [];
    
    const results = [];
    for (const item of contents) {
      if (item.videoRenderer) {
        const vr = item.videoRenderer;
        const videoId = vr.videoId;
        if (!videoId) continue;
        
        const title = vr.title?.runs?.[0]?.text || vr.title?.accessibility?.accessibilityData?.label || 'Unknown';
        const artist = vr.ownerText?.runs?.[0]?.text || vr.longBylineText?.runs?.[0]?.text || 'Unknown';
        const durationStr = vr.lengthText?.simpleText || '';
        const durationMs = durationStr ? parseSimpleDuration(durationStr) : 180000;
        const coverUrl = vr.thumbnail?.thumbnails?.[0]?.url || `https://img.youtube.com/vi/${videoId}/hqdefault.jpg`;
        const publishDate = vr.publishedTimeText?.simpleText || 'Recently';
        const viewCount = vr.viewCountText?.simpleText || 'Interactive stream';
        
        results.push({
          videoId,
          title,
          artist,
          durationMs,
          coverUrl,
          publishDate,
          viewCount
        });
        if (results.length >= 15) break;
      }
    }
    return results;
  } catch (e) {
    console.error("[Scraper] Live search scraping failed, using curated fallbacks.", e.message);
    return null;
  }
}

// Real YouTube Search API Secure Proxy with Scraper and Curated Fallbacks
app.get('/api/search', async (req, res) => {
  const query = req.query.q || '';
  const trimmed = query.trim();
  if (trimmed.length === 0) {
    return res.json([]);
  }

  // Support any of the common Google/YouTube/Gemini API key names configured in the Secrets panel
  const apiKey = process.env.YOUTUBE_API_KEY || process.env.GOOGLE_API_KEY || process.env.GEMINI_API_KEY;
  if (!apiKey || apiKey === 'YOUR_YOUTUBE_API_KEY' || apiKey.trim() === '') {
    console.warn("[YouTube Proxy] API key empty/missing from .env! Deploying live web-scraper fallback...");
    const liveResults = await scrapeYouTubeSearch(trimmed);
    if (liveResults && liveResults.length > 0) {
      return res.json(liveResults);
    }
    
    // Scraper failed or rate-limited: deploy matching Curated Index catalog search
    const lowerQ = trimmed.toLowerCase();
    const matches = CURATED_FALLBACKS.filter(item => 
      item.title.toLowerCase().includes(lowerQ) || 
      item.artist.toLowerCase().includes(lowerQ)
    );
    const backupList = matches.length > 0 ? matches : CURATED_FALLBACKS;
    return res.json(backupList);
  }

  try {
    // 1) Query Google YouTube search.list
    const searchUrl = `https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=15&q=${encodeURIComponent(query)}&type=video&key=${apiKey}`;
    const searchData = await fetchJSON(searchUrl);
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
    let detailsMap = {};
    try {
      const detailsData = await fetchJSON(videoDetailsUrl);
      (detailsData.items || []).forEach(v => {
        detailsMap[v.id] = {
          duration: parseISO8601Duration(v.contentDetails.duration),
          viewCount: formatViewCount(v.statistics.viewCount)
        };
      });
    } catch (errDetails) {
      console.error("[YouTube Proxy] Failed to fetch video details, continuing with defaults:", errDetails.message);
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
    console.error("[YouTube Proxy] Upstream API call failed, falling back to live scraper:", err.message || err);
    const fallbackResults = await scrapeYouTubeSearch(trimmed);
    if (fallbackResults && fallbackResults.length > 0) {
      return res.json(fallbackResults);
    }
    res.json(CURATED_FALLBACKS);
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

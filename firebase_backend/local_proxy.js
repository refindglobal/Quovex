const express = require('express');
const cors = require('cors');
const axios = require('axios');

const app = express();
app.use(cors());
app.use(express.json({ limit: '50mb' }));

const TARGET_URL = 'https://api-dopkbhqrgq-uc.a.run.app';

app.all('*', async (req, res) => {
    try {
        const url = `${TARGET_URL}${req.originalUrl}`;
        const headers = { ...req.headers };
        delete headers.host;
        
        const response = await axios({
            method: req.method,
            url: url,
            headers: headers,
            data: req.body,
            validateStatus: () => true
        });

        res.status(response.status).json(response.data);
    } catch (err) {
        console.error('Proxy error:', err.message);
        res.status(500).json({ error: err.message });
    }
});

const PORT = 5001;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`Quovex Local Proxy listening on http://0.0.0.0:${PORT} -> ${TARGET_URL}`);
});

import React from 'react';
import {
  AbsoluteFill,
  Img,
  interpolate,
  spring,
  useCurrentFrame,
  useVideoConfig,
} from 'remotion';

const BG = '#030807';
const EMERALD = '#00C896';
const GLOW = '#00FF9D';
const CYAN = '#22F6FF';
const WHITE = '#F7FFFC';

const clamp = {extrapolateLeft: 'clamp' as const, extrapolateRight: 'clamp' as const};

const ease = (frame: number, start: number, end: number, from = 0, to = 1) =>
  interpolate(frame, [start, end], [from, to], clamp);

const svgBolt = (
  <svg width="430" height="600" viewBox="0 0 430 600" fill="none">
    <defs>
      <linearGradient id="bolt" x1="80" y1="40" x2="350" y2="570" gradientUnits="userSpaceOnUse">
        <stop stopColor="#EFFFFB" />
        <stop offset="0.28" stopColor="#00FFCC" />
        <stop offset="0.72" stopColor="#00C896" />
        <stop offset="1" stopColor="#00A67A" />
      </linearGradient>
      <filter id="boltGlow" x="-80%" y="-40%" width="260%" height="180%">
        <feGaussianBlur stdDeviation="16" result="b" />
        <feMerge><feMergeNode in="b" /><feMergeNode in="SourceGraphic" /></feMerge>
      </filter>
    </defs>
    <path
      d="M77 35 L349 40 L245 219 L382 230 L129 575 L181 322 L48 322 Z"
      fill="url(#bolt)"
      stroke="#73FFF0"
      strokeWidth="5"
      filter="url(#boltGlow)"
    />
  </svg>
);

const ticks = Array.from({length: 12}, (_, i) => i);

const Particles: React.FC<{opacity: number; intensity?: number}> = ({opacity, intensity = 1}) => {
  const frame = useCurrentFrame();
  const dots = Array.from({length: 48}, (_, i) => {
    const a = i * 137.5;
    const radius = 210 + ((i * 31) % 270);
    const speed = 0.25 + ((i % 7) * 0.045);
    const x = 960 + Math.cos((a * Math.PI) / 180) * radius;
    const y = 510 + Math.sin((a * Math.PI) / 180) * radius * 0.58;
    const drift = Math.sin(frame * speed + i) * 22 * intensity;
    const size = 1.5 + (i % 4) * 0.8;
    const o = (0.25 + (i % 5) * 0.12) * opacity;
    return <circle key={i} cx={x + drift} cy={y + drift * 0.3} r={size} fill={i % 3 === 0 ? CYAN : GLOW} opacity={o} />;
  });
  return <svg width="1920" height="1080" style={{position: 'absolute', inset: 0}}>{dots}</svg>;
};

const EnergyRing: React.FC<{progress: number; opacity: number}> = ({progress, opacity}) => {
  const circumference = 2 * Math.PI * 300;
  return (
    <svg width="760" height="760" viewBox="0 0 760 760" style={{position: 'absolute', left: 580, top: 80, opacity}}>
      <defs>
        <linearGradient id="ringGrad" x1="0" y1="0" x2="1" y2="1">
          <stop stopColor={CYAN}/><stop offset="0.45" stopColor={GLOW}/><stop offset="1" stopColor={EMERALD}/>
        </linearGradient>
        <filter id="ringGlow"><feGaussianBlur stdDeviation="9" result="blur"/><feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
      </defs>
      <circle cx="380" cy="380" r="300" fill="none" stroke="#0D2921" strokeWidth="18" />
      <circle
        cx="380" cy="380" r="300" fill="none" stroke="url(#ringGrad)" strokeWidth="9"
        strokeLinecap="round" filter="url(#ringGlow)"
        strokeDasharray={circumference}
        strokeDashoffset={circumference * (1 - progress)}
        transform="rotate(-90 380 380)"
      />
      <circle cx="380" cy="380" r="322" fill="none" stroke="#00FF9D" strokeOpacity=".18" strokeWidth="2" />
    </svg>
  );
};

const Clock: React.FC<{progress: number; opacity: number}> = ({progress, opacity}) => (
  <svg width="650" height="650" viewBox="0 0 650 650" style={{position:'absolute', left:635, top:135, opacity, transform:`scale(${0.94 + progress * 0.06})`}}>
    <defs>
      <radialGradient id="clockFace">
        <stop stopColor="#0A2920"/>
        <stop offset="1" stopColor="#020B08"/>
      </radialGradient>
      <filter id="clockGlow"><feGaussianBlur stdDeviation="5" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
    </defs>
    <circle cx="325" cy="325" r="245" fill="url(#clockFace)" stroke="#00FF9D" strokeWidth="5" opacity=".92" filter="url(#clockGlow)"/>
    <circle cx="325" cy="325" r="222" fill="none" stroke="#0D3B2E" strokeWidth="3"/>
    {ticks.map((i) => {
      const angle = (i * 30 - 90) * Math.PI / 180;
      const x1 = 325 + Math.cos(angle) * 192;
      const y1 = 325 + Math.sin(angle) * 192;
      const x2 = 325 + Math.cos(angle) * 214;
      const y2 = 325 + Math.sin(angle) * 214;
      return <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke={CYAN} strokeWidth="9" strokeLinecap="round" opacity={i / 12 < progress ? 1 : .12} />;
    })}
    <line x1="325" y1="325" x2="420" y2="365" stroke="#00FFD0" strokeWidth="14" strokeLinecap="round" />
    <line x1="325" y1="325" x2="325" y2="220" stroke="#00FFD0" strokeWidth="12" strokeLinecap="round" />
    <circle cx="325" cy="325" r="15" fill={WHITE} stroke={GLOW} strokeWidth="6"/>
  </svg>
);

const SegmentedShell: React.FC<{progress: number; opacity: number}> = ({progress, opacity}) => {
  const segments = [
    {r: 300, start: -135, end: -25, color: '#00FF9D'},
    {r: 300, start: -25, end: 65, color: '#D8E0EA'},
    {r: 300, start: 65, end: 155, color: '#00D8B0'},
    {r: 300, start: 155, end: 245, color: '#0C5860'},
  ];
  const arc = (r: number, start: number, end: number) => {
    const p = (deg: number) => [380 + r * Math.cos(deg * Math.PI/180), 380 + r * Math.sin(deg * Math.PI/180)];
    const [x1,y1] = p(start); const [x2,y2] = p(end);
    return `M ${x1} ${y1} A ${r} ${r} 0 0 1 ${x2} ${y2}`;
  };
  return (
    <svg width="760" height="760" viewBox="0 0 760 760" style={{position:'absolute',left:580,top:80,opacity}}>
      <defs>
        <filter id="shellGlow"><feGaussianBlur stdDeviation="7" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
        <linearGradient id="metal" x1="0" y1="0" x2="1" y2="1"><stop stopColor="#F8FFFF"/><stop offset=".3" stopColor="#7B8998"/><stop offset=".6" stopColor="#26303B"/><stop offset="1" stopColor="#E9F1FA"/></linearGradient>
      </defs>
      {segments.map((s,i) => <path key={i} d={arc(s.r, s.start, s.start + (s.end-s.start)*progress)} fill="none" stroke={s.color} strokeWidth="66" strokeLinecap="round" filter="url(#shellGlow)" opacity={.95}/>) }
      <circle cx="380" cy="380" r="338" fill="none" stroke="url(#metal)" strokeWidth="3" opacity=".45"/>
    </svg>
  );
};

export const QuovexLogoAnimation: React.FC = () => {
  const frame = useCurrentFrame();
  const {fps} = useVideoConfig();

  const startGlow = ease(frame, 0, 18);
  const ring = ease(frame, 14, 42);
  const clock = ease(frame, 35, 68);
  const shell = ease(frame, 58, 86);
  const bolt = spring({frame: frame - 78, fps, config: {damping: 16, stiffness: 120, mass: 0.7}});
  const boltIn = ease(frame, 78, 100) * Math.max(0, Math.min(1, bolt));
  const qComplete = ease(frame, 94, 122);
  const text = spring({frame: frame - 116, fps, config: {damping: 14, stiffness: 100, mass: 0.8}});
  const textIn = Math.max(0, Math.min(1, text));
  const tagline = ease(frame, 140, 160);
  const finalGlow = ease(frame, 154, 191);
  const exit = ease(frame, 178, 191, 1, 0);

  const pulse = 1 + Math.sin(frame / 10) * 0.018 * qComplete;
  const logoScale = 0.92 + 0.08 * qComplete;

  return (
    <AbsoluteFill style={{background: BG, color: WHITE, fontFamily: 'Inter, Arial, sans-serif', overflow:'hidden'}}>
      <AbsoluteFill style={{opacity: exit}}>
        {/* Background atmosphere */}
        <div style={{position:'absolute', inset:0, background:'radial-gradient(circle at 50% 45%, rgba(0,200,150,.13), transparent 34%), radial-gradient(circle at 50% 50%, rgba(0,255,157,.055), transparent 52%)'}} />
        <Particles opacity={0.8 * (startGlow + finalGlow)} intensity={1.4}/>
        <div style={{position:'absolute', inset:0, opacity:.2, backgroundImage:'linear-gradient(rgba(0,255,157,.04) 1px, transparent 1px), linear-gradient(90deg, rgba(0,255,157,.04) 1px, transparent 1px)', backgroundSize:'80px 80px'}} />

        {/* Stage 1: dark start / energy seed */}
        <div style={{position:'absolute', left:'50%', top:510, width:18, height:18, transform:`translate(-50%,-50%) scale(${1 + startGlow * 8})`, borderRadius:'50%', background:GLOW, boxShadow:`0 0 20px ${GLOW}, 0 0 80px ${EMERALD}`, opacity:startGlow}} />

        {/* Stage 2: energy ring */}
        <EnergyRing progress={ring} opacity={ring} />

        {/* Stage 3: clock formation */}
        <Clock progress={clock} opacity={clock} />

        {/* Stage 4: segmented shell */}
        <SegmentedShell progress={shell} opacity={shell} />

        {/* Stage 5: lightning strike */}
        <div style={{position:'absolute', left:755, top:245, opacity:boltIn, transform:`translateY(${(1-boltIn)*170}px) scale(${.75 + .25*boltIn}) rotate(${(1-boltIn)*-8}deg)`, transformOrigin:'center'}}>
          {svgBolt}
        </div>

        {/* Stage 6: final Q logo asset */}
        <div style={{position:'absolute', inset:0, display:'flex', justifyContent:'center', alignItems:'center', opacity:qComplete, transform:`scale(${logoScale * pulse})`}}>
          <Img src="/quovex-logo.png" style={{width:780, height:'auto', objectFit:'contain', filter:`drop-shadow(0 0 18px rgba(0,255,157,.55)) drop-shadow(0 0 70px rgba(0,200,150,.22))`}} />
        </div>

        {/* Controlled flash at logo completion */}
        <div style={{position:'absolute', inset:0, background:`radial-gradient(circle at 50% 47%, rgba(255,255,255,${Math.max(0, 0.18 * (1 - Math.abs(frame-118)/14))}), transparent 22%)`, pointerEvents:'none'}} />

        {/* Stage 7/8: text + tagline */}
        <div style={{position:'absolute', left:0, right:0, bottom:138, textAlign:'center', opacity:textIn, transform:`translateY(${(1-textIn)*75}px)`, letterSpacing:'10px', fontSize:68, fontWeight:800, color:WHITE, textShadow:`0 0 8px rgba(255,255,255,.75), 0 0 28px rgba(0,255,157,.5)`}}>
          QUOVEX
        </div>
        <div style={{position:'absolute', left:'50%', bottom:78, width:720, height:54, transform:`translateX(-50%) scaleX(${tagline})`, transformOrigin:'center', opacity:tagline}}>
          <div style={{position:'absolute', inset:0, border:'2px solid rgba(0,255,157,.85)', clipPath:'polygon(5% 0,95% 0,100% 50%,95% 100%,5% 100%,0 50%)', boxShadow:`0 0 20px rgba(0,255,157,.4) inset, 0 0 20px rgba(0,255,157,.3)`}} />
          <div style={{position:'absolute', inset:0, display:'flex', alignItems:'center', justifyContent:'center', gap:36, color:WHITE, fontSize:18, fontWeight:600, letterSpacing:9}}>
            <span>FOCUS</span><span style={{color:GLOW, fontSize:12}}>●</span><span>LEARN</span><span style={{color:GLOW, fontSize:12}}>●</span><span>MASTER</span>
          </div>
        </div>

        {/* Final glow */}
        <div style={{position:'absolute', left:'50%', top:'47%', width:760, height:760, transform:'translate(-50%,-50%)', borderRadius:'50%', background:`radial-gradient(circle, rgba(0,255,157,${.12*finalGlow}), transparent 62%)`, filter:'blur(14px)', opacity:finalGlow}} />
      </AbsoluteFill>
    </AbsoluteFill>
  );
};

import {Composition, registerRoot} from 'remotion';
import {QuovexLogoAnimation} from './QuovexLogoAnimation';

export const RemotionRoot = () => {
  return (
    <Composition
      id="QuovexLogoAnimation"
      component={QuovexLogoAnimation}
      durationInFrames={192}
      fps={60}
      width={1920}
      height={1080}
      defaultProps={{}}
    />
  );
};

registerRoot(RemotionRoot);

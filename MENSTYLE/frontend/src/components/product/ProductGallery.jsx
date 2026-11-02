import { useState } from 'react';
import { resolveImageUrl } from '../../utils/imageUrl';

export default function ProductGallery({ images = [], name }) {
  const [active, setActive] = useState(0);
  const current = images[active] || images[0];
  return <div className="gallery"><div className="gallery-main">{current ? <img src={resolveImageUrl(current)} alt={name} /> : <div className="image-placeholder">MENSTYLE</div>}</div><div className="gallery-thumbs">{images.map((image, index) => <button type="button" key={image} className={index === active ? 'active' : ''} onClick={() => setActive(index)}><img src={resolveImageUrl(image)} alt={`${name} ${index + 1}`} /></button>)}</div></div>;
}

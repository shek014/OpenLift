"""Builds the app's sounds from scratch, so that they're original and free to use.

Run from the repository root: python3 tools/make_sounds.py
"""
import math
import struct
import wave

RATE = 22050
OUT = "app/src/main/res/raw"


def tone(freq, length, decay, partials=((1.0, 1.0),), attack=0.004):
    """A note that starts quickly and fades out. Partials are (frequency ratio, loudness) pairs."""
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = min(1.0, t / attack) * math.exp(-t / decay)
        samples.append(envelope * sum(level * math.sin(2 * math.pi * freq * ratio * t) for ratio, level in partials))
    return samples


def silence(length):
    return [0.0] * int(RATE * length)


def mix(*tracks):
    """Lays tracks over each other, each given as (start time, samples)."""
    end = max(int(start * RATE) + len(samples) for start, samples in tracks)
    out = [0.0] * end
    for start, samples in tracks:
        offset = int(start * RATE)
        for i, value in enumerate(samples):
            out[offset + i] += value
    return out


def save(name, samples, volume=0.8):
    peak = max(abs(s) for s in samples) or 1.0
    with wave.open(f"{OUT}/{name}.wav", "wb") as file:
        file.setnchannels(1)
        file.setsampwidth(2)
        file.setframerate(RATE)
        file.writeframes(b"".join(struct.pack("<h", int(32767 * volume * s / peak)) for s in samples))


warm = ((1.0, 1.0), (2.0, 0.3), (3.0, 0.1))
save("sound_chime", mix((0.0, tone(1318.5, 0.9, 0.35, warm)), (0.14, tone(1760.0, 1.1, 0.45, warm))))
save("sound_bell", tone(880.0, 1.6, 0.6, ((1.0, 1.0), (2.0, 0.5), (2.76, 0.35), (5.4, 0.15))))
save("sound_ding", tone(1568.0, 0.8, 0.25, warm))
beep = tone(1000.0, 0.14, 1.0, ((1.0, 1.0), (3.0, 0.2)), attack=0.005)
save("sound_beep", mix((0.0, beep), (0.22, beep)) + silence(0.05))
short = tone(1250.0, 0.09, 1.0, ((1.0, 1.0), (3.0, 0.2)), attack=0.004)
save("sound_triple_beep", mix((0.0, short), (0.15, short), (0.30, short)) + silence(0.05))
save(
    "sound_fanfare",
    mix(
        (0.00, tone(1046.5, 0.5, 0.25, warm)),
        (0.10, tone(1318.5, 0.5, 0.25, warm)),
        (0.20, tone(1568.0, 0.5, 0.25, warm)),
        (0.30, tone(2093.0, 1.0, 0.45, warm)),
    ),
)

# Sound Files for Tetris Game

This folder contains all audio files used in the Tetris game.

## Folder Structure

### `/music/` 
Place background music files here (WAV format recommended):
- `background.wav` - Main game background music (loops continuously)
- `menu.wav` - Menu screen background music (loops continuously)  
- `gameover.wav` - Game over music (plays once)

### `/effects/`
Place sound effect files here (WAV format):
- `move.wav` - Piece movement sound (short beep)
- `rotate.wav` - Piece rotation sound (short tone)
- `drop.wav` - Hard drop sound (quick swoosh)
- `line_clear.wav` - Line clear sound (success chime)
- `level_up.wav` - Level up sound (celebration tone)
- `pause.wav` - Pause/resume sound (notification beep)
- `gameover.wav` - Game over sound effect (sad tone)

## File Requirements for Best Performance
- **Format**: WAV files (16-bit, 44100 Hz recommended)
- **Channels**: Mono or Stereo both supported  
- **File size**: Keep under 10MB for smooth loading
- **Music files**: Can be longer (30 seconds to 3 minutes)
- **Effect files**: Should be short (0.1 to 1 second)

## Current Status
- ✅ Sound system implemented and working
- ✅ Volume controls (0-100%) for music and effects
- ✅ Hotkeys: M (music toggle), Ctrl+S (effects toggle)
- ✅ Volume hotkeys: +/- (effects), Ctrl++/- (music)
- ❌ Music files: Please add background.wav, menu.wav, gameover.wav
- ❌ Effect files: Please add the 7 sound effects listed above

## Usage Notes
- If files are missing, game will fall back to system beep sounds
- Music will loop automatically when playing
- Volume settings are saved during the game session
- All audio can be controlled through in-game settings menu

## Testing
Once you add the WAV files:
1. Run the game: `java TetrisGame`
2. Test music with M key (toggle on/off)
3. Test volume with +/- keys  
4. Test effects by moving pieces, clearing lines, etc.
5. Access full audio settings in the Config Screen
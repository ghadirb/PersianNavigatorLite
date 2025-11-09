# Vosk Persian Model

This is a placeholder for the Vosk Persian speech recognition model.

## Real Model Requirements:
- Model size: ~40MB
- Files needed:
  - am/final.mdl (acoustic model)
  - graph/HCLG.fst (grammar)
  - conf/mfcc.conf (configuration)
  - conf/online.conf (online configuration)

## Download Instructions:
1. Download from: https://alphacephei.com/vosk/models
2. Extract to: assets/vosk-model-small-fa-0.4/
3. The model will work offline without internet

## Current Status:
- Using Google Speech as fallback
- Ready for Vosk integration when model is available
- System automatically switches when model is detected

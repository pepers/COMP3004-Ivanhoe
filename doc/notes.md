# Notes

### Table of Contents:
- [Feb. 8th](notes.md#feb-8th)


### Feb. 8th
- public class SerializableObject implements Serializable
  - use ObjectOutputStream, with instanceOf on the receiving end to determine type of object
  - objects:
    - error
    - clientAction
    - card
    - playerState
      - # cards in hand
      - display - ArrayList of card objects
      - displayTotal
    - gameState
      - tournamentColour
      - players - ArrayListof playerState objects
    - chatMessage - String message
    
- Deck data structure
  - deck stack
  - discard stack
  - draw() - if empty, copy discard to deck, then shuffle()
  - private shuffle() - shuffles deck 
  - deck() constructor
    - add all card objects for full deck
    - call shuffle()

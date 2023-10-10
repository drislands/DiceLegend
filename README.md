# Dice Legend
Dice Legend is a bot that connects to an IRC channel to facilitate elemental 
duels between two players at a time. Flameo, Hotman!

# Configuring
TBD when I get this part actually programmed in, instead of being hardcoded.

# Usage
## Registration
Once Dice Legend connects to your IRC channel, each user who wants to 
participate must register by sending the message `?register`. Once the bot
confirms your registration, you can proceed to the next step!
## Challenges
A registered player may challenge another registered player by sending the
message `?challenge <user>`, replacing `<user>` with the username of your foe.

If you have been challenged, you may reject the challenge with `?reject`, or 
you may accept with `?accept`. Once a challenge has been accepted, the game is
on!

## Duelling
Dice Legend will announce that a fight has begun, and inform users they should
DM it to specify the move they wish to use. Refer to the Moves section for a 
list of moves and their descriptions.

Once each player has DM'd the bot with their chosen move, the bot will describe
the results of each to the channel, including resulting health. If one or more
players have 0 health, the winner will be announced; otherwise the players 
should repeat by sending their next chosen move in a DM.

## Forfeiting
TBD once I get this in.

# Discord
That's next!

# Moves
Each move will have two or more Traits, as well as other statistics about them
including effects they create.
## Traits
### Speed
Fast, Slow. Fast moves will always go before moves without this trait. Slow 
moves will always go after moves without this trait. If a move gets both 
traits, such as with Air Acceleration, Fast overrides Slow. In the event both 
moves have the same speed, whoever goes first is randomly determined.
### Element
Fire, Earth, Water, Air. Each element has a loose theme, and their moves tend 
to have better synergy. Fire is more focused on pure damage, Earth tries to
turtle up with defense before unleashing, Water slows enemies and heals itself,
Air plays tricks that can boost your other moves.
### Type
Attack, Effect, Trap. Each move is exactly one of these, and currently, each 
element has exactly one move of each type. Attacks do direct damage, Effects
boost your own moves, and Traps afflict your opponent.
### Range
Contact, Ranged. Only Attack move have these, and each will have exactly one.
These traits don't have any inherent function -- they are instead checked by
your effects, and your opponent's traps.
### Lingering
Stacking, Persistent. Only Effect moves have these, but not all will have even
one of them. Stacking means you can have more than one instance of the effect
at the same time. Persistent means that the effect will remain after 
triggering. Currently, there are no effects with the Persistent trait.
## Damage stats
Each move will have all of these stats, but only Attack moves will make use of
most of them. Here is a list with descriptions:
* Hit Dice
  * The number of dice that are rolled when determining damage.
* Auto Hits
  * Additional dice, but instead of being rolled, they automatically hit.
* Damage per Hit
  * The amount of damage dealt per die that hits.
* Self Damage
  * The amount of damage the move deals to you. NOT multiplied by hits.
* Heal per Hit
  * The amount of healing the move does to you.
* Bonus Damage
  * Final modifier for damage -- can be positive or negative, but will never
  reduce damage to less than 0. No healing your opponent!

## Move List
### Fire
#### Rocket Punch
* Contact, Fast, Fire, Attack

* 3 hit dice, 1 damage per hit

Strike fast and hard! With the Fast trait, this attack has a 50/50 chance of 
going before any wall your opponent plays.

#### Fire Fists
* Slow, Fire, Effect

* 1 self damage
* EFFECT: Adds 2 auto-hits to next Contact Attack

Jack up your next Contact Attack with 2 guaranteed hits, at the low cost of 1
damage now! Careful you don't use it twice in a row by accident, though -- this
effect does NOT stack!

#### Flame Wall
* Fast, Fire, Trap
  
* EFFECT: -1 bonus damage to foe's Ranged, non-Earth Attack this turn
* EFFECT: 2 self damage to foe's Contact Attack this turn

Put up a sheet of flames to defend yourself! If your foe tries to strike up
close, they'll feel the heat with 2 free damage! If they try to strike from
range, they'll see their effectiveness fizzle out...unless it's mighty Earth!
A good all-around trap.

### Earth
#### Rock Throw
* Ranged, Earth, Attack

* 1 hit die, 1 damage per hit

A simple rock, thrown with force. It doesn't have a lot of oomph by itself, but
buff yourself with a few stacks of Clump Earth and get ready to rock your foe!

#### Clump Earth
* Stacking, Slow, Earth, Effect

* EFFECT: Adds 2 hit dice to next Ranged Earth Attack

Pack the earth together to ready yourself for a mighty blow! Put as many stacks
on as you like -- but don't forget to attack before your foe notices and puts
a stop to you!

#### Stone Wall
* Fast, Earth, Trap

* EFFECT: -2 bonus damage to foe's non-Fast Attack this turn
* EFFECT: increase the damage mitigation by 1 for each stack of Clump Earth

Raise a mighty wall to defend yourself from your foe! It grows in effectiveness
for every stack of Clump Earth waiting, and won't burn through those stacks
when you use this move -- but beware a fast attack from your foe, which will
always beat this otherwise-sturdy defense!

### Water
#### Water Jet
* Ranged, Water, Attack

* 2 hit dice, 1 damage per hit

Fire a jet of water at your foe. It's not powerful by itself, but combo with
Blessed Fountain to get yourself back in the fight!

#### Blessed Fountain
* Stacking, Slow, Water, Effect

* EFFECT: Add 2 heal per hit to next Water Attack

If your health is getting too low, summon the Blessed Fountain! A few stacks
combined with a well-placed Water Jet can get you right back in fighting shape!

#### Ice Wall
* Fast, Water, Trap

* EFFECT: -1 bonus damage to foe's Attack this turn
* END-OF-TURN: if foe did not Attack, heal for 1 per stack of Blessed Fountain

Summon a towering wall of ice to defend yourself! It will mitigate any attack,
no matter its traits -- as long as you go first! And if your foe doesn't 
attack, get some free healing from your Blessed Fountain stacks without wasting
them or your turn!

### Air
#### Gale Strike
* Contact, Air, Attack

* 1 auto-hit, 1 damage per hit

Guarantee a hit with this attack! It won't do much by itself, but combo with
other effects to bring out more potential!

#### Air Acceleration
* Slow, Air, Effect

* EFFECT: Add 1 hit dice and Fast trait to next Attack

Make any strike with blistering speed after you use this effect! You'll boost
any attack to Fast, as well as adding a hit die! Don't get greedy, though --
this effect does NOT stack!

#### Winds of Evasion
* Fast, Air, Trap

* EFFECT: -1 bonus damage to foe's Attack this turn
* EFFECT: 1 self-damage to foe's Contact Attack this turn 

Surround yourself with winds that will help you to dodge out of the way of an
incoming attack -- and if the foe gets too close, to strike back! A great 
all-around defense, as long as your opponent is attacking in the first place!
## go

Play Go versus a computer opponent.

This program implements [Monte-Carlo tree search](https://en.wikipedia.org/wiki/Monte_Carlo_tree_search) to decide the computer's moves. In a Monte-Carlo tree each node represents a state of the game and each edge represents a move. This algorithm consists of four steps. In the first step it descends the tree to the direction of the most promissing move until a leaf node is reached. Then child nodes are added to the selected leaf node, each one of which corresponds to a possible move from the game state that the leaf node represents. In the third step, starting from each child node's state, the game is played out until the end, by deciding each player's move based on a policy. This policy is created using knowledge from games of high level Go players. Finally, moving up from the child node to the root of the tree, it updates the information about the result of the playout in every node encountered. 

## Usage

First clone the repo:
```
  git clone https://github.com/theovasi/go
  cd go/src
```

Compile and run:
```
  javac Controller.java
  java Controller
```

## License

This project is licensed under the MIT License - see the LICENSE file for details

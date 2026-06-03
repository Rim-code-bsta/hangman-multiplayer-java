# ♛ The Gallows: Royal Court Edition

A Moroccan-themed multiplayer Hangman game built in Java using TCP sockets, multithreading, and Swing GUI design.

> In the royal courts of Morocco, words carried power.  
> This project reimagines the classic Hangman game inside a Moroccan palace inspired by zellige mosaics, Moorish architecture, and the elegance of traditional royal courts.

---

## 🎥 Demo

▶ **Watch the project demonstration here:**

[Demo Video](https://drive.google.com/file/d/1rQtA6deBXwyCphBjCgVfrhYJA6gxcxMt/view?usp=sharing)

---

## 📖 Project Overview

The Gallows: Royal Court Edition is a two-player networked Hangman game developed as part of a Computer Networks course project.

The application follows a client-server architecture:

- One player becomes the **Word Setter**
- One player becomes the **Word Guesser**
- Additional users join as **Spectators**
- The server manages all game logic and communication
- Clients interact through a fully custom Swing graphical interface

Unlike a traditional Hangman game, the visual design is inspired by Moroccan royal architecture:

- 🕌 Moorish arches
- 🔶 Zellige tile patterns
- 🎩 Traditional Moroccan Tarbouch
- 💚 Emerald, Gold, and Crimson royal palette

---

## ✨ Features

### Networking
- TCP Socket communication
- Multi-client support
- Dedicated thread per client
- Server-side game state management
- Broadcast messaging system

### Gameplay
- Word Setter role
- Word Guesser role
- Spectator mode
- Duplicate guess detection
- Automatic win/loss detection
- Game reset functionality

### GUI
- Built entirely using Java Swing
- Custom Graphics2D rendering
- CardLayout-based screen navigation
- Animated game state updates
- Moroccan-inspired visual design

### Audio
- Real `.wav` sound effects
- Correct guess applause sound
- Wrong guess sound effect
- Non-blocking audio playback through daemon threads

---

## 🏗 Architecture

### Main Components

| Component | Responsibility |
|------------|----------------|
| ChatServer | Accepts connections, manages game state, broadcasts updates |
| ClientHandler | Handles communication with a single client |
| ChatClient | Client-side networking engine |
| HangmanChatGUI | Graphical interface and game visualization |
| SoundEngine | Audio playback system |

---

## 🔄 System Architecture Diagram

[Mermaid Diagram representative](https://drive.google.com/file/d/1vps23p5Zq5qRzibgVckpGOB7UACFQZsd/view?usp=sharing)
+
[Mermaid Diagram 2nd](https://drive.google.com/file/d/1mTc5WQ-bI2QyRRXSyg0SaXFtOR1h6Xpb/view?usp=sharing)
---

## 🛠 Technologies Used

- Java
- Java Swing
- Graphics2D
- TCP Sockets
- Multithreading
- Object-Oriented Programming

---

## 📂 Project Structure

```text
src/
│
├── ChatServer.java
├── ChatClient.java
├── HangmanChatGUI.java
├── SoundEngine.java
│
├── Applause.wav
└── wrong.wav
```

---

## 🚀 Running the Project

### Start the Server

```bash
java ChatServer 8989
```

### Start the Client

```bash
java HangmanChatGUI
```

### Connect

Use:

```text
Server: localhost
Port: 8989
```

Open multiple client windows to simulate multiple players.

---

## 🎓 Academic Context

This project was developed for this course in the university of New Orleans now part of LSU:

**CSCI 4311 – Computer Networks**

The objective was to demonstrate:

- Socket Programming
- Client-Server Architecture
- Multithreading
- Network Communication Protocols
- GUI Integration with Networking

---

## 🏆 Results

- Final Grade: **120 / 100**
- Bonus Features Implemented
- Fully customized graphical interface
- Multiplayer network gameplay
- Sound effects and thematic visual design

---

## 👩‍💻 Author

**Rim Bousta**

Computer Science Student • Software Engineering • Artificial Intelligence

---

*"A single hidden word holds all the power."*

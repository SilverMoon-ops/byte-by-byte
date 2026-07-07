let userScore = 0;
let compScore = 0;
let mode = "computer"; // "computer" or "player"
let currentTurn = 1; // used only in 2-player mode
let player1Choice = null;

const choices = document.querySelectorAll(".choice");
const msg = document.querySelector("#msg");
const turnIndicator = document.querySelector("#turn-indicator");

const userScorePara = document.querySelector("#user-score");
const compScorePara = document.querySelector("#comp-score");
const userLabel = document.querySelector("#user-label");
const compLabel = document.querySelector("#comp-label");

const vsComputerBtn = document.querySelector("#vsComputerBtn");
const vsPlayerBtn = document.querySelector("#vsPlayerBtn");
const themeBtns = document.querySelectorAll(".theme-btn");

const genCompChoice = () => {
  const options = ["rock", "paper", "scissors"];
  return options[Math.floor(Math.random() * 3)];
};

const drawGame = () => {
  msg.innerText = "Game was a Draw. Play again.";
  msg.style.backgroundColor = "var(--bg-color)";
};

// returns true if c1 beats c2, false if c2 beats c1, null if draw
const decideWinner = (c1, c2) => {
  if (c1 === c2) return null;
  if (c1 === "rock") return c2 === "scissors";
  if (c1 === "paper") return c2 === "rock";
  return c2 === "paper"; // c1 === "scissors"
};

const showWinner = (winnerIsP1, p1Choice, p2Choice, p1Label, p2Label) => {
  if (winnerIsP1) {
    userScore++;
    userScorePara.innerText = userScore;
    msg.innerText = `${p1Label} wins! ${p1Choice} beats ${p2Choice}`;
    msg.style.backgroundColor = "green";
  } else {
    compScore++;
    compScorePara.innerText = compScore;
    msg.innerText = `${p2Label} wins! ${p2Choice} beats ${p1Choice}`;
    msg.style.backgroundColor = "crimson";
  }
};

const resetTurn = () => {
  currentTurn = 1;
  player1Choice = null;
  turnIndicator.innerText = mode === "player" ? "Player 1's turn" : "";
};

const playGame = (choice) => {
  if (mode === "computer") {
    const compChoice = genCompChoice();
    const result = decideWinner(choice, compChoice);
    if (result === null) drawGame();
    else showWinner(result, choice, compChoice, "You", "Computer");
  } else {
    if (currentTurn === 1) {
      player1Choice = choice;
      currentTurn = 2;
      turnIndicator.innerText = "Player 2's turn";
      msg.innerText = "Player 1 has chosen. Player 2, go!";
      msg.style.backgroundColor = "var(--bg-color)";
    } else {
      const result = decideWinner(player1Choice, choice);
      if (result === null) drawGame();
      else showWinner(result, player1Choice, choice, "Player 1", "Player 2");
      resetTurn();
    }
  }
};

choices.forEach((choice) => {
  choice.addEventListener("click", () => {
    playGame(choice.getAttribute("id"));
  });
});

vsComputerBtn.addEventListener("click", () => {
  mode = "computer";
  vsComputerBtn.classList.add("active");
  vsPlayerBtn.classList.remove("active");
  userLabel.innerText = "You";
  compLabel.innerText = "Comp";
  userScore = 0;
  compScore = 0;
  userScorePara.innerText = 0;
  compScorePara.innerText = 0;
  resetTurn();
  msg.innerText = "Play your move";
  msg.style.backgroundColor = "var(--bg-color)";
});

vsPlayerBtn.addEventListener("click", () => {
  mode = "player";
  vsPlayerBtn.classList.add("active");
  vsComputerBtn.classList.remove("active");
  userLabel.innerText = "Player 1";
  compLabel.innerText = "Player 2";
  userScore = 0;
  compScore = 0;
  userScorePara.innerText = 0;
  compScorePara.innerText = 0;
  resetTurn();
  msg.innerText = "Player 1, choose your move";
  msg.style.backgroundColor = "var(--bg-color)";
});

themeBtns.forEach((btn) => {
  btn.addEventListener("click", () => {
    document.body.className = "";
    if (btn.dataset.theme !== "theme-default") {
      document.body.classList.add(btn.dataset.theme);
    }
  });
});

resetTurn();

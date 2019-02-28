package minesweeper.frame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import minesweeper.neural.Neural;
import neurolib.neural.TrainingSet;

@SuppressWarnings("serial")
public class Frame extends JPanel implements ActionListener, ChangeListener {

	JFrame frame;

	public boolean train = false;
	public final boolean delay = true;
	public boolean ai = true;
	public boolean alwaysWin = true;
	public int bombCount = 50;
	public int gameCount = -1;
	

	public Frame() {
		frame = new JFrame("Minesweeper");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setSize(1217, 820);
		frame.setVisible(true);
		frame.add(this);

		init();
	}

	Timer timer;

	Neural neural;
	ArrayList<TrainingSet> trainingSets = new ArrayList<TrainingSet>();

	public boolean wait = false;
	
	public int neuroOffsetX = 950;
	public int neuroOffsetY = 310;

	public int offsetX = 10;
	public int offsetY = 10;
	public int gridX = 15;
	public int gridY = 25;
	public int neuroGridSize = 5;

	public int[][] currentNeuroGrid;
	public double currentNeuroValue;

	public int wins = 0;

	public int speed = 1;
	public int AIX = -1;
	public int AIY = -1;

	public int[][] grid;
	public int[][] gridView;
//	public int[][] neuroGrid;

	public double[][] values;

	public boolean end = false;

	BufferedImage cell;
	BufferedImage flag;
	BufferedImage bomb;
	BufferedImage selectedBomb;
	BufferedImage cell1;
	BufferedImage cell2;
	BufferedImage cell3;
	BufferedImage cell4;
	BufferedImage cell5;
	BufferedImage cell6;
	BufferedImage cell7;
	BufferedImage cell8;

	public JSlider speedSlider;

	private void init() {

		neural = new Neural("minesweeper14", neuroGridSize);

		this.setLayout(null);

		speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 1);
		speedSlider.addChangeListener(this);
		speedSlider.setLocation(10, 767);
		speedSlider.setSize(920, 20);
		speedSlider.setVisible(true);

		this.add(speedSlider);
		
		speedSlider.requestFocus();

		try {
			cell = ImageIO.read(getClass().getClassLoader().getResource("minesweeper/assets/Cell.png"));
			flag = ImageIO.read(getClass().getClassLoader().getResource("minesweeper/assets/Flag.png"));
			bomb = ImageIO.read(getClass().getClassLoader().getResource("minesweeper/assets/Bomb.png"));
			selectedBomb = ImageIO.read(getClass().getClassLoader().getResource("minesweeper/assets/SelectedBomb.png"));
			cell1 = ImageIO.read(getClass().getClassLoader().getResource("minesweeper/assets/1.png"));
			cell2 = ImageIO.read(getClass().getClassLoader().getResource("minesweeper/assets/2.png"));
			cell3 = ImageIO.read(getClass().getClassLoader().getResource("minesweeper/assets/3.png"));
			cell4 = ImageIO.read(getClass().getClassLoader().getResource("minesweeper/assets/4.png"));
			cell5 = ImageIO.read(getClass().getClassLoader().getResource("minesweeper/assets/5.png"));
			cell6 = ImageIO.read(getClass().getClassLoader().getResource("minesweeper/assets/6.png"));
			cell7 = ImageIO.read(getClass().getClassLoader().getResource("minesweeper/assets/7.png"));
			cell8 = ImageIO.read(getClass().getClassLoader().getResource("minesweeper/assets/8.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		grid = new int[gridX][];
		for (int x = 0; x < gridX; x++) {
			grid[x] = new int[gridY];
			for (int y = 0; y < gridY; y++) {
				grid[x][y] = 0;
			}
		}

		int i = 0;
		while (i < bombCount) {
			int x = new Random().nextInt(gridX);
			int y = new Random().nextInt(gridY);
			if (grid[x][y] == 0) {
				grid[x][y] = 1;
				i++;
			}
		}

		gridView = new int[gridX][];
		for (int x = 0; x < gridX; x++) {
			gridView[x] = new int[gridY];
			for (int y = 0; y < gridY; y++) {
				gridView[x][y] = -1;
			}
		}

		values = new double[gridX][];
		for (int x = 0; x < gridX; x++) {
			values[x] = new double[gridY];
			for (int y = 0; y < gridY; y++) {
				values[x][y] = 2.0;
			}
		}

		frame.addMouseListener(new mouseClick());
		frame.addKeyListener(new keyPress());

		timer = new Timer(1000 / 60, this);
		timer.start();

		if (ai) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					int games = 0;
					int rPosX;
					int rPosY;
					while (true) {
						rPosX = new Random().nextInt(gridX);
						rPosY = new Random().nextInt(gridY);
						click(rPosX, rPosY);
						if (alwaysWin) {
							wait = true;
							while (!end) {
								AI(false);
								double bestValue = 1;
								int[] pos = new int[2];
								for (int x = 0; x < gridX; x++) {
									for (int y = 0; y < gridY; y++) {
										if (values[x][y] <= bestValue
												&& (gridView[x][y] == -1 || gridView[x][y] == 11)) {
											bestValue = values[x][y];
											pos[0] = x;
											pos[1] = y;
										}
										if (values[x][y] != 2.0 && values[x][y] > 0.8 && gridView[x][y] == -1) {
											setFlag(x, y);
										} else if (values[x][y] <= 0.8 && gridView[x][y] == 11) {
											removeFlag(x, x);
										}
									}
								}
								click(pos[0], pos[1]);
							}
						}
						if (alwaysWin == false || testWin()) {
							if (alwaysWin == true) {
								clear();
							}
							wait = false;
							click(rPosX, rPosY);
							while (!end) {
								AI(delay);
								if (delay) {
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								double bestValue = 1;
								int[] pos = new int[2];
								for (int x = 0; x < gridX; x++) {
									for (int y = 0; y < gridY; y++) {
										if (values[x][y] <= bestValue
												&& (gridView[x][y] == -1 || gridView[x][y] == 11)) {
											bestValue = values[x][y];
											pos[0] = x;
											pos[1] = y;
										}
										if (values[x][y] != 2.0 && values[x][y] > 0.8 && gridView[x][y] == -1) {
											if (train) {
												if (grid[x][y] == 0) {
													for (int c = 0; c < 15; c++) {
														train(x, y);
													}
												}
											}
											setFlag(x, y);
											if (train) {
												if (grid[x][y] == 0) {
													for (int c = 0; c < 15; c++) {
														train(x, y);
													}
												}
											}
										} else if (values[x][y] <= 0.8 && gridView[x][y] == 11) {
											if (train) {
												if (grid[x][y] == 0) {
													for (int c = 0; c < 15; c++) {
														train(x, y);
													}
												}
											}
											removeFlag(x, x);
											if (train) {
												if (grid[x][y] == 0) {
													for (int c = 0; c < 15; c++) {
														train(x, y);
													}
												}
											}
										}
									}
								}
								if (train) {
									if (grid[pos[0]][pos[1]] == 1) {
										for (int c = 0; c < 30; c++) {
											train(pos[0], pos[1]);
										}
									}
								}
								click(pos[0], pos[1]);
							}
							games++;
							AIX = -1;
							AIY = -1;
							if (delay) {
								currentNeuroGrid = null;
								try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
						reset();
						end = false;
						if (games >= gameCount && gameCount != -1 && delay == false) {
							System.out.println("Games: " + games);
							System.out.println("Wins: " + wins);
							System.out.println("Chance: " + Math.round(((wins / (double) games) * 10000)) / 100d + "%");
							games = 0;
							wins = 0;
							if (train) {
								neural.train(trainingSets);
							}
							trainingSets.clear();
						}
					}
				}
			}).start();
		}
	}

	public void AI(boolean delay) {
		if (delay && speed != 10) {
			for (int x = 0; x < gridX; x++) {
				for (int y = 0; y < gridY; y++) {
					values[x][y] = 2.0;
					AIX = -1;
					AIY = -1;
				}
			}
		}

		for (int y = 0; y < gridY; y++) {
			for (int x = 0; x < gridX; x++) {
				if ((gridView[x][y] == -1 || gridView[x][y] == 11) && isBorder(x, y)) {
					boolean d = false;
					if (delay && values[x][y] == 2.0 && speed != 10) {
						d = true;
						AIX = x;
						AIY = y;

					} else if (speed == 10) {
						AIX = -1;
						AIY = -1;
					}
					currentNeuroGrid = getNeuroGrid(x, y);
					currentNeuroValue = neural.getValue(currentNeuroGrid);
					values[x][y] = currentNeuroValue;
					if (d) {
						try {
							Thread.sleep((int) (Math.pow((10 - speed), 2.8) * 4));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (train) {
						train(x, y);
					}
				} else {
					values[x][y] = 2.0;
				}
			}
		}
	}

	public void trainBorder(int times) {
		for (int x = 0; x < gridX; x++) {
			for (int y = 0; y < gridY; y++) {
				if ((gridView[x][y] == -1 || gridView[x][y] == 11) && isBorder(x, y)) {
					for (int i = 0; i < times; i++) {
						train(x, y);
					}
				}
			}
		}
	}

	public void train(int posX, int posY) {
		ArrayList<Double> input = new ArrayList<Double>();
		int[][] neuroGrid = getNeuroGrid(posX, posY);
		for (int x = 0; x < neuroGridSize; x++) {
			for (int y = 0; y < neuroGridSize; y++) {
				if (neuroGrid[x][y] == 11) {
					input.add(0.0);
					input.add(1.0);
				} else if (neuroGrid[x][y] == -1 || neuroGrid[x][y] == 11) {
					input.add(0.0);
					input.add(0.0);
				} else if (neuroGrid[x][y] == 0) {
					input.add(1.0);
					input.add(0.0);
				} else {
					input.add(1.0);
					input.add((neuroGrid[x][y] * 2) / 10d);
				}
			}
		}

		ArrayList<Double> goodOutputs = new ArrayList<Double>();

		goodOutputs.add((double) grid[posX][posY]);

		TrainingSet set = new TrainingSet(input, goodOutputs);

		trainingSets.add(set);
//		if (grid[posX][posY] == 9) {
//			for (int i = 0; i < 10; i++) {
//				trainingSets.add(set);
//			}
//		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2D = (Graphics2D) g;
		g2D.setColor(Color.LIGHT_GRAY);
		g2D.drawRect(offsetX - 1, offsetY - 1, gridX * 30 + 1, gridY * 30 + 1);
		g2D.fillRect((gridX * 30) + offsetX + 9, offsetY - 1, gridX * 30 + 2, gridY * 30 + 2);

		if (gridView != null) {
			for (int x = 0; x < gridX; x++) {
				for (int y = 0; y < gridY; y++) {
					if (alwaysWin && wait) {
						g2D.drawImage(cell, offsetX + 30 * x, offsetY + 30 * y, 30, 30, null);
						continue;
					}
					if (gridView[x][y] == -1) {
						g2D.drawImage(cell, offsetX + 30 * x, offsetY + 30 * y, 30, 30, null);
					}
					if (gridView[x][y] == 0) {
						g2D.drawRect(offsetX + 30 * x - 1, offsetY + 30 * y - 1, 31, 31);
					}
					if (gridView[x][y] == 1) {
						g2D.drawImage(cell1, offsetX + 30 * x, offsetY + 30 * y, 30, 30, null);
						g2D.drawRect(offsetX + 30 * x - 1, offsetY + 30 * y - 1, 31, 31);
					}
					if (gridView[x][y] == 2) {
						g2D.drawImage(cell2, offsetX + 30 * x, offsetY + 30 * y, 30, 30, null);
						g2D.drawRect(offsetX + 30 * x - 1, offsetY + 30 * y - 1, 31, 31);
					}
					if (gridView[x][y] == 3) {
						g2D.drawImage(cell3, offsetX + 30 * x, offsetY + 30 * y, 30, 30, null);
						g2D.drawRect(offsetX + 30 * x - 1, offsetY + 30 * y - 1, 31, 31);
					}
					if (gridView[x][y] == 4) {
						g2D.drawImage(cell4, offsetX + 30 * x, offsetY + 30 * y, 30, 30, null);
						g2D.drawRect(offsetX + 30 * x - 1, offsetY + 30 * y - 1, 31, 31);
					}
					if (gridView[x][y] == 5) {
						g2D.drawImage(cell5, offsetX + 30 * x, offsetY + 30 * y, 30, 30, null);
						g2D.drawRect(offsetX + 30 * x - 1, offsetY + 30 * y - 1, 31, 31);
					}
					if (gridView[x][y] == 6) {
						g2D.drawImage(cell6, offsetX + 30 * x, offsetY + 30 * y, 30, 30, null);
						g2D.drawRect(offsetX + 30 * x - 1, offsetY + 30 * y - 1, 31, 31);
					}
					if (gridView[x][y] == 7) {
						g2D.drawImage(cell7, offsetX + 30 * x, offsetY + 30 * y, 30, 30, null);
						g2D.drawRect(offsetX + 30 * x - 1, offsetY + 30 * y - 1, 31, 31);
					}
					if (gridView[x][y] == 8) {
						g2D.drawImage(cell8, offsetX + 30 * x, offsetY + 30 * y, 30, 30, null);
						g2D.drawRect(offsetX + 30 * x - 1, offsetY + 30 * y - 1, 31, 31);
					}
					if (gridView[x][y] == 9) {
						g2D.drawImage(bomb, offsetX + 30 * x, offsetY + 30 * y, 30, 30, null);
						g2D.drawRect(offsetX + 30 * x - 1, offsetY + 30 * y - 1, 31, 31);
					}
					if (gridView[x][y] == 10) {
						g2D.drawImage(selectedBomb, offsetX + 30 * x, offsetY + 30 * y, 30, 30, null);
						g2D.drawRect(offsetX + 30 * x - 1, offsetY + 30 * y - 1, 31, 31);
					}
					if (gridView[x][y] == 11) {
						g2D.drawImage(flag, offsetX + 30 * x, offsetY + 30 * y, 30, 30, null);
					}
//					g2D.setColor(Color.BLACK);
//					g2D.drawString(gridView[x][y] + "", offsetX + 30 * x + 2, offsetY + 30 * y + 12);
				}
			}
		}

		g2D.drawRect(neuroOffsetX - 1, neuroOffsetY - 1, neuroGridSize * 30 + 1, neuroGridSize * 30 + 1);

		for (int x = 0; x < neuroGridSize; x++) {
			for (int y = 0; y < neuroGridSize; y++) {

				if (alwaysWin && wait || currentNeuroGrid == null || speed == 10 || delay == false) {
					g2D.drawImage(cell, neuroOffsetX + 30 * x, neuroOffsetY + 30 * y, 30, 30, null);
					continue;
				}

				int value = currentNeuroGrid[x][y];

				if (value == -1) {
					g2D.drawImage(cell, neuroOffsetX + 30 * x, neuroOffsetY + 30 * y, 30, 30, null);
				}
				if (value == 0) {
					g2D.drawRect(neuroOffsetX + 30 * x - 1, neuroOffsetY + 30 * y - 1, 31, 31);
				}
				if (value == 1) {
					g2D.drawImage(cell1, neuroOffsetX + 30 * x, neuroOffsetY + 30 * y, 30, 30, null);
					g2D.drawRect(neuroOffsetX + 30 * x - 1, neuroOffsetY + 30 * y - 1, 31, 31);
				}
				if (value == 2) {
					g2D.drawImage(cell2, neuroOffsetX + 30 * x, neuroOffsetY + 30 * y, 30, 30, null);
					g2D.drawRect(neuroOffsetX + 30 * x - 1, neuroOffsetY + 30 * y - 1, 31, 31);
				}
				if (value == 3) {
					g2D.drawImage(cell3, neuroOffsetX + 30 * x, neuroOffsetY + 30 * y, 30, 30, null);
					g2D.drawRect(neuroOffsetX + 30 * x - 1, neuroOffsetY + 30 * y - 1, 31, 31);
				}
				if (value == 4) {
					g2D.drawImage(cell4, neuroOffsetX + 30 * x, neuroOffsetY + 30 * y, 30, 30, null);
					g2D.drawRect(neuroOffsetX + 30 * x - 1, neuroOffsetY + 30 * y - 1, 31, 31);
				}
				if (value == 5) {
					g2D.drawImage(cell5, neuroOffsetX + 30 * x, neuroOffsetY + 30 * y, 30, 30, null);
					g2D.drawRect(neuroOffsetX + 30 * x - 1, neuroOffsetY + 30 * y - 1, 31, 31);
				}
				if (value == 6) {
					g2D.drawImage(cell6, neuroOffsetX + 30 * x, neuroOffsetY + 30 * y, 30, 30, null);
					g2D.drawRect(neuroOffsetX + 30 * x - 1, neuroOffsetY + 30 * y - 1, 31, 31);
				}
				if (value == 7) {
					g2D.drawImage(cell7, neuroOffsetX + 30 * x, neuroOffsetY + 30 * y, 30, 30, null);
					g2D.drawRect(neuroOffsetX + 30 * x - 1, neuroOffsetY + 30 * y - 1, 31, 31);
				}
				if (value == 8) {
					g2D.drawImage(cell8, neuroOffsetX + 30 * x, neuroOffsetY + 30 * y, 30, 30, null);
					g2D.drawRect(neuroOffsetX + 30 * x - 1, neuroOffsetY + 30 * y - 1, 31, 31);
				}
				if (value == 9) {
					g2D.drawImage(bomb, neuroOffsetX + 30 * x, neuroOffsetY + 30 * y, 30, 30, null);
					g2D.drawRect(neuroOffsetX + 30 * x - 1, neuroOffsetY + 30 * y - 1, 31, 31);
				}
				if (value == 10) {
					g2D.drawImage(selectedBomb, neuroOffsetX + 30 * x, neuroOffsetY + 30 * y, 30, 30, null);
					g2D.drawRect(neuroOffsetX + 30 * x - 1, neuroOffsetY + 30 * y - 1, 31, 31);
				}
				if (value == 11) {
					g2D.drawImage(flag, neuroOffsetX + 30 * x, neuroOffsetY + 30 * y, 30, 30, null);
				}
			}
		}

		if (!(alwaysWin && wait || currentNeuroGrid == null || speed == 10 || delay == false)) {
			Color c = new Color((float) currentNeuroValue, (float) (1 - currentNeuroValue), 0);
			g2D.setColor(c);
			g2D.fillRect(neuroOffsetX + 210, neuroOffsetY + 60, 30, 30);
		} else {
			Color c = Color.GRAY;
			g2D.setColor(c);
			g2D.fillRect(neuroOffsetX + 210, neuroOffsetY + 60, 30, 30);
		}

		if (values != null) {
			for (int x = 0; x < gridX; x++) {
				for (int y = 0; y < gridY; y++) {
					double value = values[x][y];
					if (value == 2 || (alwaysWin && wait)) {
						Color c = Color.GRAY;
						if (!(alwaysWin && wait) && (gridView[x][y] == -1 || gridView[x][y] == 11) && isBorder(x, y)) {
							c = Color.BLUE;
						}
						g2D.setColor(c);
						g2D.fillRect((30 * gridX) + offsetX + 30 * x + 11, offsetY + 30 * y + 1, 28, 28);
					} else {
						Color c = new Color((float) value, (float) (1 - value), 0);
						g2D.setColor(c);
						g2D.fillRect((30 * gridX) + offsetX + 30 * x + 11, offsetY + 30 * y + 1, 28, 28);
					}
				}
			}
		}
		
		
		if (!wait && delay && speed != 10 && values != null) {
			Color green = new Color(0, 1, 0, 0.5f);
			Color red = new Color(1, 0, 0, 0.5f);
			double bestValue = 1;
			int bestX = -1;
			int bestY = -1;
			for (int x = 0; x < gridX; x++) {
				for (int y = 0; y < gridY; y++) {
					if (values[x][y] <= bestValue
							&& (gridView[x][y] == -1 || gridView[x][y] == 11)) {
						bestValue = values[x][y];
						bestX = x;
						bestY = y;
					}
					if (values[x][y] > 0.8 && values[x][y] != 2 && gridView[x][y] == -1) {
						g2D.setColor(red);
						g2D.fillRect(offsetX + 30 * x, offsetY + 30 * y, 30, 30);
					}
				}
			}
			g2D.setColor(green);
			if (bestX != -1 && bestY != -1) {
				g2D.fillRect(offsetX + 30 * bestX, offsetY + 30 * bestY, 30, 30);
			}
		}

		g2D.setColor(Color.BLUE);
		g2D.setStroke(new BasicStroke(3));
		
		if (AIX != -1 && AIY != -1) {
			g2D.drawRect(offsetX + 30 * AIX, offsetY + 30 * AIY, 30, 30);
			int mX1 = 0;
			int mX2 = 0;
			if ((AIX - (int) Math.floor((double) neuroGridSize / 2)) < 0) {
				mX1 = (AIX - (int) Math.floor((double) neuroGridSize / 2));
			} else if ((AIX + (int) Math.ceil((double) neuroGridSize / 2)) > gridX) {
				mX2 = (AIX + (int) Math.ceil((double) neuroGridSize / 2)) - gridX;
			}
			int mY1 = 0;
			int mY2 = 0;
			if ((AIY - (int) Math.floor((double) neuroGridSize / 2)) < 0) {
				mY1 = (AIY - (int) Math.floor((double) neuroGridSize / 2));
			} else if ((AIY + (int) Math.ceil((double) neuroGridSize / 2)) > gridY) {
				mY2 = (AIY + (int) Math.ceil((double) neuroGridSize / 2)) - gridY;
			}
			g2D.drawRect(offsetX + 30 * (AIX - (int) Math.floor((double) neuroGridSize / 2) - mX1),
					offsetY + 30 * (AIY - (int) Math.floor(neuroGridSize / 2d) - mY1), 30 * (neuroGridSize - mX2 + mX1),
					30 * (neuroGridSize - mY2 + mY1));
			g2D.drawLine(offsetX + 30 * (AIX + 3 - mX2), offsetY + 30 * AIY + 15, offsetX + 30 * AIX + 460,
					offsetY + 30 * AIY + 15);
			g2D.drawRect(offsetX + 30 * AIX + 460, offsetY + 30 * AIY, 30, 30);
		}
		
		g2D.drawRect(neuroOffsetX + 30 * 2, neuroOffsetY + 30 * 2, 30, 30);
		g2D.drawRect(neuroOffsetX, neuroOffsetY, 30 * neuroGridSize, 30 * neuroGridSize);
		g2D.drawLine(neuroOffsetX + 30 * neuroGridSize, neuroOffsetY + 75, neuroOffsetX + 30 * (neuroGridSize + 2),
				neuroOffsetY + 75);
		g2D.drawRect(neuroOffsetX + 30 * (neuroGridSize + 2), neuroOffsetY + 30 * 2, 30, 30);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	private class keyPress extends JComponent implements KeyListener {

		@SuppressWarnings("static-access")
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == e.VK_BACK_SPACE) {
				reset();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {

		}

		@Override
		public void keyTyped(KeyEvent e) {

		}

	}

	private class mouseClick extends JComponent implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			int mouseX = e.getX();
			int mouseY = e.getY();
			mouseX -= offsetX + 3;
			mouseY -= offsetY + 27;
			int posX = mouseX / 30;
			int posY = mouseY / 30;
			if (posX < gridX && posY < gridY && mouseX >= 0 && mouseY >= 0) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					click(posX, posY);
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					if (!end) {
						gridView[posX][posY] = 11;
					}
//					neuroGrid = getNeuroGrid(posX, posY);
				}

				// DEBUG:
				if (e.getButton() == MouseEvent.BUTTON2) {
					for (int x = 0; x < gridX; x++) {
						for (int y = 0; y < gridY; y++) {
							if (grid[x][y] == 0) {
								click(x, y);
							} else {
								setFlag(x, y);
							}
						}
					}
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}
	}

	public void click(int posX, int posY) {
		if ((gridView[posX][posY] == -1 || gridView[posX][posY] == 11) && !end) {
			if (grid[posX][posY] == 1) {
//				for (int i = 0; i < 10; i++) {
//					train(posX, posY);
//				}
				gridView[posX][posY] = 10;
				fail();
			} else {
				int bombCount = getBombCount(posX, posY);
				gridView[posX][posY] = bombCount;
				if (bombCount == 0) {
					if (posX - 1 >= 0) {
						click(posX - 1, posY);
					}
					if (posX + 1 < gridX) {
						click(posX + 1, posY);
					}
					if (posY - 1 >= 0) {
						click(posX, posY - 1);
					}
					if (posY + 1 < gridY) {
						click(posX, posY + 1);
					}
				}
			}
		}
		if (testWin() && !end) {
			wins++;
			end = true;
		}
	}

	public void setFlag(int posX, int posY) {
		gridView[posX][posY] = 11;
		if (testWin() && !end) {
			wins++;
			end = true;
		}
	}

	public void removeFlag(int posX, int posY) {
		if (gridView[posX][posY] == 11) {
			gridView[posX][posY] = -1;
		}
	}

	public boolean testWin() {
		boolean win = true;
		for (int x = 0; x < gridX; x++) {
			for (int y = 0; y < gridY; y++) {
				if (grid[x][y] == 0 && (gridView[x][y] == -1 || gridView[x][y] == 11)) {
					win = false;
				} else if (grid[x][y] == 1 && gridView[x][y] != 11) {
					win = false;
				}
			}
		}
		return win;
	}

	public void fail() {
		end = true;
		for (int y = 0; y < gridY; y++) {
			for (int x = 0; x < gridX; x++) {
				if (grid[x][y] == 1 && gridView[x][y] != 10) {
					gridView[x][y] = 9;
				}
			}
		}
//		reset();
	}

	public int getBombCount(int posX, int posY) {
		int bombCount = 0;
		int bx = posX - 1;
		int by = posY - 1;
		for (int x = bx; x < bx + 3; x++) {
			for (int y = by; y < by + 3; y++) {
				if (x >= 0 && y >= 0 && x < gridX && y < gridY && grid[x][y] == 1) {
					bombCount++;
				}
			}
		}
		return bombCount;
	}

	public int[][] getNeuroGrid(int posX, int posY) {
		int[][] neuroGrid = new int[neuroGridSize][];
		int bx = posX - (int) ((neuroGridSize - 1) / 2);
		int by = posY - (int) ((neuroGridSize - 1) / 2);
		for (int x = 0; x < neuroGridSize; x++) {
			neuroGrid[x] = new int[neuroGridSize];
			for (int y = 0; y < neuroGridSize; y++) {
				if (x + bx >= 0 && y + by >= 0 && x + bx < gridX && y + by < gridY) {
					neuroGrid[x][y] = gridView[x + bx][y + by];
				} else {
					neuroGrid[x][y] = -1;
				}
			}
		}
		return neuroGrid;
	}

	public boolean isBorder(int posX, int posY) {
		int boderCount = 0;
		if (posX - 1 >= 0) {
			if (gridView[posX - 1][posY] == -1) {
				boderCount++;
			}
		} else {
			boderCount++;
		}
		if (posY - 1 >= 0) {
			if (gridView[posX][posY - 1] == -1) {
				boderCount++;
			}
		} else {
			boderCount++;
		}
		if (posX + 1 < gridX) {
			if (gridView[posX + 1][posY] == -1) {
				boderCount++;
			}
		} else {
			boderCount++;
		}
		if (posY + 1 < gridY) {
			if (gridView[posX][posY + 1] == -1) {
				boderCount++;
			}
		} else {
			boderCount++;
		}
		if (boderCount < 4) {
			return true;
		} else {
			return false;
		}
	}

	public void reset() {
		for (int x = 0; x < gridX; x++) {
			for (int y = 0; y < gridY; y++) {
				grid[x][y] = 0;
			}
		}

//		bombCount = new Random().nextInt(10) + 30;

		int i = 0;
		while (i < bombCount) {
			int x = new Random().nextInt(gridX);
			int y = new Random().nextInt(gridY);
			if (grid[x][y] == 0) {
				grid[x][y] = 1;
				i++;
			}
		}

		for (int x = 0; x < gridX; x++) {
			for (int y = 0; y < gridY; y++) {
				gridView[x][y] = -1;
			}
		}

		for (int x = 0; x < gridX; x++) {
			values[x] = new double[gridY];
			for (int y = 0; y < gridY; y++) {
				values[x][y] = 2.0;
			}
		}

		end = false;
	}

	public void clear() {
		for (int x = 0; x < gridX; x++) {
			for (int y = 0; y < gridY; y++) {
				gridView[x][y] = -1;
			}
		}

		for (int x = 0; x < gridX; x++) {
			values[x] = new double[gridY];
			for (int y = 0; y < gridY; y++) {
				values[x][y] = 2.0;
			}
		}
		end = false;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		speed = speedSlider.getValue();

	}
}

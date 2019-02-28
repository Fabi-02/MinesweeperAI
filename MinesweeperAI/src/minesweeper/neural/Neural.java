package minesweeper.neural;

import java.io.IOException;
import java.util.ArrayList;

import neurolib.neural.Network;
import neurolib.neural.Train;
import neurolib.neural.TrainingSet;

public class Neural {

	Network network;
	Train train;
	String name;
	int grid;

	public Neural(String name, int grid) {
		this.name = name;
		this.grid = grid;
		try {
			network = Network.load("network", name + "-" + grid);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (network == null) {
			network = new Network(grid * grid * 2, grid * grid, grid, 1);
			save();
		}

		train = new Train(network);
	}

	private void save() {
		try {
			network.save("network", name + "-" + grid, 10);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	double trains = 4;

	public void train(ArrayList<TrainingSet> trainSet) {
		System.out.println("Train");
		for (TrainingSet set : trainSet) {
			train.addTrainingSet(set);
		}
//		train.train(trainSet.size(), 1 / Math.pow(10, trains));
		train.train(trainSet.size(), 0.000001);
		train.clear();
		save();
		System.out.println("Finishtrain");
//		trains += 0.2;
//		if (trains > 10) {
//			trains = 10;
//		}
	}

	public double getValue(int[][] grid) {
		ArrayList<Double> input = new ArrayList<>();

		for (int x = 0; x < grid.length; x++) {
			for (int y = 0; y < grid[x].length; y++) {
				if (grid[x][y] == 11) {
					input.add(0.0);
					input.add(1.0);
				} else if (grid[x][y] == -1) {
					input.add(0.0);
					input.add(0.0);
				} else if (grid[x][y] == 0) {
					input.add(1.0);
					input.add(0.0);
				} else {
					input.add(1.0);
					input.add((grid[x][y] * 2) / 10d);
				}
			}
		}

		ArrayList<Double> output = network.run(input);

		return output.get(0);
	}
}

package pl.opole.uni.cs.unifDL.Filo.view;

import pl.opole.uni.cs.unifDL.Filo.controller.Solver;

import javax.swing.*;
import java.awt.*;

public class StatisticWindow {

	private JFrame frame;
	private JFrame activeFrame;
	private JLabel labelNumberOfConstants, labelNumberOfUserVariables, labelMaxNumberOfComputationVariables,
			labelNumberOfDecidedDuringPreprocessing, labelNumberOfDecidedByComputingShortcuts, labelSolvingTime;

	
	public StatisticWindow(Solver solver, JFrame activeFrame) {
		frame = new JFrame("Statistics");
		frame.setSize(400, 220);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				closeStatisticWindow();
			}
		});
		this.activeFrame = activeFrame;
		SpringLayout layout = new SpringLayout();
		frame.setLayout(layout);
		setLocation(activeFrame);

		labelNumberOfConstants = new JLabel("Number of constants " + solver.getNumberOfConstants(),
				SwingConstants.LEFT);
		labelNumberOfUserVariables = new JLabel("Number of user variables " + solver.getNumberOfUserVariables(),
				SwingConstants.LEFT);
		labelMaxNumberOfComputationVariables = new JLabel(
				"Max number of computation variables " + solver.getMaxNumberOfComputationVariables(),
				SwingConstants.LEFT);
		labelNumberOfDecidedDuringPreprocessing = new JLabel(
				"Number of attempts decided during preprocessing " + solver.getNumberOfDecidedDuringPreprocessing(),
				SwingConstants.LEFT);
		labelNumberOfDecidedByComputingShortcuts = new JLabel(
				"Number of attempts decided by computing shortcuts " + solver.getNumberOfDecidedByComputingShortcuts(),
				SwingConstants.LEFT);
		labelSolvingTime = new JLabel("Solving time " + (solver.getSolvingTime()/(1000000)) + " ms", SwingConstants.LEFT);

		layout.putConstraint(SpringLayout.NORTH, labelNumberOfConstants, 10, SpringLayout.NORTH, frame);
		layout.putConstraint(SpringLayout.WEST, labelNumberOfConstants, 10, SpringLayout.WEST, frame);
		layout.putConstraint(SpringLayout.NORTH, labelNumberOfUserVariables, 10, SpringLayout.SOUTH,
				labelNumberOfConstants);
		layout.putConstraint(SpringLayout.WEST, labelNumberOfUserVariables, 10, SpringLayout.WEST, frame);
		layout.putConstraint(SpringLayout.NORTH, labelMaxNumberOfComputationVariables, 10, SpringLayout.SOUTH,
				labelNumberOfUserVariables);
		layout.putConstraint(SpringLayout.WEST, labelMaxNumberOfComputationVariables, 10, SpringLayout.WEST, frame);
		layout.putConstraint(SpringLayout.NORTH, labelNumberOfDecidedDuringPreprocessing, 10, SpringLayout.SOUTH,
				labelMaxNumberOfComputationVariables);
		layout.putConstraint(SpringLayout.WEST, labelNumberOfDecidedDuringPreprocessing, 10, SpringLayout.WEST, frame);
		layout.putConstraint(SpringLayout.NORTH, labelNumberOfDecidedByComputingShortcuts, 10, SpringLayout.SOUTH,
				labelNumberOfDecidedDuringPreprocessing);
		layout.putConstraint(SpringLayout.WEST, labelNumberOfDecidedByComputingShortcuts, 10, SpringLayout.WEST, frame);
		
		
		layout.putConstraint(SpringLayout.NORTH, labelSolvingTime, 10, SpringLayout.SOUTH,
				labelNumberOfDecidedByComputingShortcuts);
		layout.putConstraint(SpringLayout.WEST, labelSolvingTime, 10, SpringLayout.WEST, frame);

		frame.add(labelNumberOfConstants);
		frame.add(labelNumberOfUserVariables);
		frame.add(labelMaxNumberOfComputationVariables);
		frame.add(labelNumberOfDecidedDuringPreprocessing);
		frame.add(labelNumberOfDecidedByComputingShortcuts);
		frame.add(labelSolvingTime);
	}

	public void showStatisticWindow() {
		frame.setVisible(true);
		activeFrame.setEnabled(false);
	}

	public void closeStatisticWindow() {
		activeFrame.setEnabled(true);
		frame.dispose();
	}

	private void setLocation(JFrame activeFrame) {
		Rectangle bounds = activeFrame.getBounds();

		int x = bounds.x + (bounds.width - frame.getWidth()) / 2;
		int y = bounds.y + (bounds.height - frame.getHeight()) / 2;

		frame.setLocation(x, y);
	}
}

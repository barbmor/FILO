package pl.opole.uni.cs.unifDL.Filo.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class loadingScreen {

	private JFrame frame;
	private JLabel solvingLabel;
	private JButton stopButton;
	private JFrame activeFrame;

	loadingScreen(MainWindow activeFrame) {
		frame = new JFrame("Solving");
		frame.setSize(300, 100);
		frame.setUndecorated(true);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		setLocation(activeFrame);
		this.activeFrame = activeFrame;

		solvingLabel = new JLabel("Loading", SwingConstants.CENTER);
		solvingLabel.setFont(new Font("Arial", Font.PLAIN, 20));
		solvingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		stopButton = new JButton("Stop");
		stopButton.setForeground(Color.RED);
		stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(null, "Do you want to stop computation?", "Confirm",
						JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					activeFrame.getSolver().setRunFlag(false);
				}
			}
		});

		panel.add(Box.createVerticalStrut(20));
		panel.add(solvingLabel);
		panel.add(Box.createVerticalStrut(10));
		panel.add(stopButton);

		frame.add(panel);
	}

	public void showLoadingScreen() {
		frame.setVisible(true);
		activeFrame.setEnabled(false);

		new Thread(() -> {
			try {
				int counter = 0;
				int maxDotsCount = 5;
				while (frame.isVisible()) {
					String dots = ".".repeat(counter % (maxDotsCount + 1));
					String space = " ".repeat(maxDotsCount - (counter % (maxDotsCount + 1)));
					solvingLabel.setText("Solving" + dots + space);
					Thread.sleep(500);
					counter++;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void closeLoadingScreen() {
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

package BeeSimulation.userpanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import BeeSimulation.agents.Hive;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunListener;
import repast.simphony.engine.environment.RunState;
import repast.simphony.userpanel.ui.UserPanelCreator;

public class UserPanel implements UserPanelCreator {

	private Optional<Hive> hive = Optional.empty();
	private Optional<JList<Long>> list = Optional.empty();
	private BearListModel listModel = new BearListModel();

	@Override
	public JPanel createPanel() {
		var panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		var bearButton = new JButton("Bear Attack");
		var label = new JLabel("Bear Attack Log (ticks)");
		list = Optional.of(new JList<Long>(listModel));

		RunEnvironment.getInstance().addRunListener(new RunListener() {

			@Override
			public void paused() {
				// Unused

			}

			@Override
			public void restarted() {
				// Unused

			}

			@Override
			public void started() {
				@SuppressWarnings("unchecked")
				Context<Hive> context = RunState.getInstance().getMasterContext();
				Stream<Hive> s = context.getObjectsAsStream(Hive.class);
				hive = Optional.of(s.findFirst().get());
				hive.get().registerPanel(UserPanel.this);
			}

			@Override
			public void stopped() {
				// Unused

			}

		});

		bearButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				UserPanel.this.hive.ifPresent(hive -> hive.bearAttack());
				logAttack();
			}

		});
		panel.add(bearButton);
		panel.add(label);
		panel.add(list.get());
		return panel;
	}

	public void logAttack() {
		long tick = (long) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		listModel.add(tick);
	}

}

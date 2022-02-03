package BeeSimulation.userpanel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

public class BearListModel extends AbstractListModel<Long> {

	private static final long serialVersionUID = 1L;
	private List<Long> list = new ArrayList<>();

	public void add(Long i) {
		list.add(i);
		fireIntervalAdded(this, list.size() - 1, list.size() - 1);
	}

	@Override
	public int getSize() {
		return this.list.size();
	}

	@Override
	public Long getElementAt(int index) {
		return this.list.get(index);
	}

}

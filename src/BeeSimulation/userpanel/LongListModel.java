package BeeSimulation.userpanel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListDataListener;

public class LongListModel implements javax.swing.ListModel<Long> {

	private List<Long> list = new ArrayList<>();

	@Override
	public int getSize() {
		return list.size();
	}

	@Override
	public Long getElementAt(int index) {
		return list.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
	}

	public void addElement(Long i) {
		list.add(i);
	}

}

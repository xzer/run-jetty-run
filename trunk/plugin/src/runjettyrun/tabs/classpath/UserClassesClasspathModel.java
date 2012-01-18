package runjettyrun.tabs.classpath;

import java.util.Iterator;

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

public class UserClassesClasspathModel extends AbstractClasspathEntry {

	public static final int USER = 1;

	public static final int CUSTOM = 4;

	private ClasspathGroup userEntries;

	private ClasspathGroup customEntries;

	private String defaultLabel;
	private String customLabel;

	public Object addEntry(Object entry) {
		if (entry instanceof ClasspathGroup) {
			if (!childEntries.contains(entry)) {
				childEntries.add((IRJRClasspathEntry)entry);
				return entry;
			}
			return null;
		}
		ClasspathEntry newEntry = createEntry((IRuntimeClasspathEntry) entry,
				null);
		Iterator<IRJRClasspathEntry> entries = childEntries.iterator();
		while (entries.hasNext()) {
			Object element = entries.next();
			if (element instanceof ClasspathGroup) {
				if (((ClasspathGroup) element).contains(newEntry)) {
					return null;
				}
			} else if (element.equals(newEntry)) {
				return null;
			}
		}
		childEntries.add(newEntry);
		return newEntry;
	}


	public Object addEntry(int entryType, IRuntimeClasspathEntry entry) {
		IRJRClasspathEntry entryParent = null;
		switch (entryType) {
		case USER:
			entryParent = getUserEntry();
			break;
		case CUSTOM:
			entryParent = getCustomEntry();
			break;
		default:
			throw new IllegalArgumentException("Unsupported entryType:"+entryType);
		}

		ClasspathEntry newEntry = createEntry(entry, entryParent);
		newEntry.setCustom(entryType == CUSTOM);

		Iterator<IRJRClasspathEntry> entries = childEntries.iterator();
		while (entries.hasNext()) {
			Object element = entries.next();
			if (element instanceof ClasspathGroup) {
				if (((ClasspathGroup) element).contains(newEntry)) {
					return null;
				}
			} else if (element.equals(newEntry)) {
				return null;
			}
		}
		if (entryParent != null) {
			((ClasspathGroup) entryParent).addEntry(newEntry, null);
		} else {
			childEntries.add(newEntry);
		}
		return newEntry;
	}

	/**
	 * Returns the entries of the given type, or an empty collection if none.
	 *
	 * @param entryType
	 * @return the entries of the given type, or an empty collection if none
	 */
	public IRJRClasspathEntry[] getEntries(int entryType) {
		switch (entryType) {
		case USER:
			if (userEntries != null) {
				return userEntries.getEntries();
			}
			break;
		case CUSTOM:
			if (customEntries != null) {
				return customEntries.getEntries();
			}
			break;
		}
		return new IRJRClasspathEntry[0];
	}

	public IRuntimeClasspathEntry[] getAllEntries() {
		IRJRClasspathEntry[] user = getEntries(USER);
		IRuntimeClasspathEntry[] all = new IRuntimeClasspathEntry[user.length];
		if (user.length > 0) {
			System.arraycopy(user, 0, all, 0, user.length);
		}
		return all;
	}


	public IRuntimeClasspathEntry[] getCustomEntries() {
		IRJRClasspathEntry[] user = getEntries(CUSTOM);
		IRuntimeClasspathEntry[] all = new IRuntimeClasspathEntry[user.length];
		if (user.length > 0) {
			System.arraycopy(user, 0, all, 0, user.length);
		}
		return all;
	}

	public void remove(Object entry) {
		childEntries.remove(entry);
	}

	public void cleanRootGroup(int type) {
		if (type == CUSTOM) {
			customEntries.removeAll();
		} else if (type == USER) {
			userEntries.removeAll();
		} else {
			throw new IllegalArgumentException("Not supported type:" + type);
		}
	}

	public ClasspathEntry createEntry(IRuntimeClasspathEntry entry,
			IRJRClasspathEntry entryParent) {
		if (entry instanceof ClasspathEntry) {
			entry = ((ClasspathEntry) entry).getDelegate();
		}
		if (entryParent == null) {
			entryParent = this;
		}
		return new ClasspathEntry(entry, entryParent);
	}

	public void clearCustom() {
		if (this.customEntries != null) {
			this.customEntries.removeAll();
		}
	}

	public void removeAll() {
		if (userEntries != null) {
			userEntries.removeAll();
		}
	}

	public void removeAll(Object[] entries) {

		for (int i = 0; i < entries.length; i++) {
			Object object = entries[i];
			if (object instanceof ClasspathEntry) {
				IRJRClasspathEntry entryParent = ((ClasspathEntry) object)
						.getParent();
				if (entryParent instanceof ClasspathGroup) {
					((ClasspathGroup) entryParent)
							.removeEntry((ClasspathEntry) object);
				} else {
					remove(object);
				}
			} else {
				remove(object);
			}
		}
	}

	private ClasspathGroup createGroupEntry(IRuntimeClasspathEntry[] entries,
			ClasspathGroup entryParent, String name, boolean canBeRemoved,
			boolean addEntry, boolean editable) {

		ClasspathGroup group = new ClasspathGroup(name, entryParent,
				canBeRemoved, editable);

		for (int i = 0; i < entries.length; i++) {
			group.addEntry(new ClasspathEntry(entries[i], group), null);
		}

		if (addEntry) {
			addEntry(group);
		}
		return group;
	}

	public void setUserEntries(IRuntimeClasspathEntry[] entries) {
		if (userEntries == null) {
			getUserEntry();
		}
		userEntries.removeAll();
		for (int i = 0; i < entries.length; i++) {
			userEntries.addEntry(new ClasspathEntry(entries[i], userEntries),
					null);
		}
	}

	public IRJRClasspathEntry getCustomEntry() {
		if (customEntries == null) {
			customEntries = createGroupEntry(new IRuntimeClasspathEntry[0],
					null, this.customLabel, false, true, true);
			customEntries.setParent(userEntries);
			customEntries.setCustom(true);
		}
		return customEntries;
	}

	public IRJRClasspathEntry getUserEntry() {
		if (userEntries == null) {
			userEntries = createGroupEntry(new IRuntimeClasspathEntry[0], null,
					this.defaultLabel, false, true, false);
		}
		return userEntries;
	}


	/**
	 * Constructs a new classpath model with root entries
	 */
	public UserClassesClasspathModel() {
		this("Default Project classpath", "User Custom classpath");
	}

	public UserClassesClasspathModel(String defaultLabel,String customLabel) {
		super();
		this.defaultLabel = defaultLabel;
		this.customLabel = customLabel;
		getUserEntry();
		getCustomEntry();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEntry#isEditable()
	 */
	public boolean isEditable() {
		return false;
	}


	public String getRealPath() {
		return toString();
	}

}

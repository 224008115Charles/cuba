package com.haulmont.cuba.web.toolkit.data.util;

import com.haulmont.cuba.web.toolkit.data.TreeTableContainer;
import com.itmill.toolkit.data.Container;
import com.itmill.toolkit.data.Item;
import com.itmill.toolkit.data.util.ContainerHierarchicalWrapper;

import java.util.*;

/**
 * User: Nikolay Gorodnov
 * Date: 05.06.2009
 */
public class TreeTableContainerWrapper
        extends ContainerHierarchicalWrapper
        implements TreeTableContainer, Container.Ordered
{
    protected Set<Object> expanded = null; // Contains expanded items ids

    protected LinkedList<Object> inline = null; // Contains visible (including children of expanded items) items ids inline

    protected Hashtable<Object, String> captions = null;

    private Object first;

    private boolean treeTableContainer;

    public TreeTableContainerWrapper(Container toBeWrapped) {
        super(toBeWrapped);
        treeTableContainer = toBeWrapped instanceof TreeTableContainer;
    }

    @Override
    public void updateHierarchicalWrapper() {
        super.updateHierarchicalWrapper();
        first = roots.peekFirst();
    }

    @Override
    protected void addToHierarchyWrapper(Object itemId) {
        if (inline == null) {
            initInline();
        }

        super.addToHierarchyWrapper(itemId);

        // Add item to the end of the list
        if (!inline.contains(itemId)) {
            inline.add(itemId);
            if (areChildrenAllowed(itemId)) {
                makeInlineElements(inline, getChildren(itemId));
            }
        }
    }

    @Override
    protected void removeFromHierarchyWrapper(Object itemId) {
        if (inline == null) {
            initInline();
        }

        boolean b = isFirstId(itemId);

        if (containsInline(itemId)) {
            List<Object> inlineChildren;
            if (areChildrenAllowed(itemId)
                    && (inlineChildren = getInlineChildren(itemId)) != null)
            {
                inline.removeAll(inlineChildren);
            }
            inline.remove(itemId);
        }

        super.removeFromHierarchyWrapper(itemId);

        if (b) {
            first = roots.iterator().next();
        }
    }

    @Override
    public boolean setParent(Object itemId, Object newParentId) {
        if (itemId == null) {
            throw new NullPointerException("Item id cannot be NULL");
        }

        if (inline == null) {
            initInline();
        }

        if (!container.containsId(itemId)) {
            return false;
        }

        final Object oldParentId = parent.get(itemId);

        if ((newParentId == null && oldParentId == null)
                || (newParentId != null && newParentId.equals(oldParentId))) {
            return true;
        }

        boolean b = super.setParent(itemId, newParentId);
        if (b)
        {
            final LinkedList<Object> inlineList = new LinkedList<Object>();
            inlineList.add(itemId);
            inlineList.addAll(getInlineChildren(itemId));

            if (containsInline(itemId)) {
                inline.removeAll(inlineList);
            }

            if (containsInline(newParentId)
                    && areChildrenAllowed(newParentId)
                    && isExpanded(newParentId))
            {
                int lastChildInlineIndex = lastInlineIndex(newParentId);
                if (lastChildInlineIndex > -1) {
                    inline.addAll(lastChildInlineIndex + 1, inlineList);
                } else {
                    inline.addAll(inlineIndex(newParentId) + 1, inlineList);
                }
            }
        }
        return b;
    }

    @Override
    public int size() {
        if (inline == null) {
            return 0;
        }
        return inline.size();
    }

    public Object nextItemId(Object itemId) {
        if (itemId == null) {
            throw new NullPointerException("Item id cannot be NULL");
        }
        if (inline == null) {
            initInline();
        }
        int index = inlineIndex(itemId);
        if (index == -1 || isLastId(itemId)) {
            return null;
        }
        return inline.get(index + 1);
    }

    public Object prevItemId(Object itemId) {
        if (itemId == null)  {
            throw new NullPointerException("Item id cannot be NULL");
        }
        if (inline == null) {
            initInline();
        }
        int index = inlineIndex(itemId);
        if (index == -1 || isFirstId(itemId)) {
            return null;
        }
        return inline.get(index - 1);
    }

    public Object firstItemId() {
        return first;
    }

    public Object lastItemId() {
        if (inline == null) {
            initInline();
        }
        return inline.peekLast();
    }

    public boolean isFirstId(Object itemId) {
        return itemId != null && itemId.equals(first);
    }

    public boolean isLastId(Object itemId) {
        return itemId != null && itemId.equals(lastItemId());
    }

    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public boolean isCaption(Object itemId) {
        if (itemId != null) {
            if (!treeTableContainer) {
                if (captions == null) {
                    captions = new Hashtable<Object, String>();
                }
                return captions.containsKey(itemId);
            } else {
                return ((TreeTableContainer) container).isCaption(itemId);
            }
        }
        throw new NullPointerException("Item id cannot be NULL");
    }

    public String getCaption(Object itemId) {
        if (itemId != null) {
            if (!treeTableContainer) {
                if (captions == null) {
                    captions = new Hashtable<Object, String>();
                }
                return captions.get(itemId);
            } else {
                return ((TreeTableContainer) container).getCaption(itemId);
            }
        }
        throw new NullPointerException("Item id cannot be NULL");
    }

    public boolean setCaption(Object itemId, String caption) {
        if (itemId != null) {
            if (!treeTableContainer) {
                if (captions == null) {
                    captions = new Hashtable<Object, String>();
                }
                if (caption != null) {
                    captions.put(itemId, caption);
                } else {
                    captions.remove(itemId);
                }
                return true;
            } else {
                return ((TreeTableContainer) container).setCaption(itemId, caption);
            }
        }
        throw new NullPointerException("Item id cannot be NULL");
    }

    public int getLevel(Object itemId) {
        if (itemId != null) {
            if (!treeTableContainer) {
                return getItemLevel(itemId);
            } else {
                return ((TreeTableContainer) container).getLevel(itemId);
            }
        }
        throw new NullPointerException("Item id cannot be NULL");
    }

    protected int getItemLevel(Object itemId) {
        Object parentId;
        if ((parentId = parent.get(itemId)) == null) {
            return 0;
        }
        return getItemLevel(parentId) + 1;
    }

    public boolean isExpanded(Object itemId) {
        if (itemId == null) {
            throw new NullPointerException("Item id cannot be NULL");
        }
        if (expanded == null) {
            initExpanded();
        }
        return expanded.contains(itemId);
    }

    public boolean setExpanded(Object itemId) {
        if (itemId == null) {
            throw new NullPointerException("Item id cannot be NULL");
        }

        if (expanded == null) {
            initExpanded();
        }

        if (inline == null) {
            initInline();
        }

        int itemIndex;
        if ((itemIndex = inlineIndex(itemId)) > -1 && areChildrenAllowed(itemId))
        {
            expanded.add(itemId);

            final List<Object> inlineChildren = getInlineChildren(itemId);
            if (inlineChildren != null) {
                inline.addAll(itemIndex + 1, inlineChildren);
            }

            return true;
        }
        return false;
    }

    public boolean setCollapsed(Object itemId) {
        if (itemId == null) {
            throw new NullPointerException("Item id cannot be NULL");
        }

        if (expanded == null) {
            initExpanded();
        }

        if (inline == null) {
            initInline();
        }

        if (containsInline(itemId) && areChildrenAllowed(itemId))
        {
            final List<Object> inlineChildren = getInlineChildren(itemId);
            if (inlineChildren != null) {
                inline.removeAll(inlineChildren);
            }

            expanded.remove(itemId);

            return true;
        }
        return false;
    }

    private void initExpanded() {
        expanded = new HashSet<Object>();
    }

    protected void initInline() {
        inline = new LinkedList<Object>();
        makeInlineElements(inline, roots);
    }

    protected LinkedList<Object> getInlineChildren(Object itemId) {
        if (areChildrenAllowed(itemId)) {
            final LinkedList<Object> inlineChildren = new LinkedList<Object>();
            if (isExpanded(itemId)) {
                makeInlineElements(inlineChildren, getChildren(itemId));
            }
            return inlineChildren;
        }
        return null;
    }

    private void makeInlineElements(final List<Object> inline, final Collection elements) {
        if (elements != null) {
            for (final Object e : elements) {
                inline.add(e);
                if (areChildrenAllowed(e) && isExpanded(e)) {
                    makeInlineElements(inline, getChildren(e));
                }
            }
        }
    }

    private boolean containsInline(Object itemId) {
        return inline.contains(itemId);
    }

    private int inlineIndex(Object itemId) {
        return inline.indexOf(itemId);
    }

    private int lastInlineIndex(Object itemId) {
        LinkedList<Object> inlineChildren = getInlineChildren(itemId);
        if (inlineChildren != null && !inlineChildren.isEmpty()) {
            return inlineIndex(inlineChildren.getLast());
        }
        return -1;
    }
}

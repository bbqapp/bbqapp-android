/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 bbqapp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bbqapp.android.view;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.bbqapp.android.R;
import org.bbqapp.android.util.AbstractListAdapter;

import java.util.ArrayList;
import java.util.List;

public class MenuAdapter extends AbstractListAdapter<MenuAdapter.Entry> {

    private OnEntryClickListener clickListener;

    private ArrayList<Entry> entries = new ArrayList<>();

    private LayoutInflater inflater;

    public MenuAdapter(final LayoutInflater inflater, final ListView listView) {
        this.inflater = inflater;
        listView.setAdapter(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (clickListener != null) {
                    clickListener.onEntryClick(listView, getItem(position), position, id);
                }
            }
        });
    }

    public void setOnEntryClickListener(OnEntryClickListener listener) {
        clickListener = listener;
    }

    public Header addHeader(String string) {
        return add(new Header(string));
    }

    public Header addHeader(int stringResource) {
        return add(new Header(stringResource));
    }

    public Header addHeader(String string, int imageResource) {
        return add(new Header(string, imageResource));
    }

    public Header addHeader(int stringResource, int imageResource) {
        return add(new Header(stringResource, imageResource));
    }

    public Header addHeader(int stringResource, Bitmap imageBitmap) {
        return add(new Header(stringResource, imageBitmap));
    }

    public Header addHeader(String string, Bitmap imageBitmap) {
        return add(new Header(string, imageBitmap));
    }

    public Header addHeader(String string, Drawable imageDrawable) {
        return add(new Header(string, imageDrawable));
    }

    public Header getHeader() {
        Entry first = !entries.isEmpty() ? entries.get(0) : null;
        return first != null && first.getType() == Type.HEADER ? (Header) first : null;
    }

    public Entry add(String string) {
        return add(new Text(string));
    }

    public Entry add(int stringResource) {
        return add(new Text(stringResource));
    }

    public Entry add(String string, int image) {
        return add(new Text(string, image));
    }

    public Entry add(String string, Drawable imageDrawable) {
        return add(new Text(string, imageDrawable));
    }

    public Entry add(int stringResource, Drawable imageDrawable) {
        return add(new Text(stringResource, imageDrawable));
    }

    public Entry add() {
        return add(new Separator());
    }

    public Entry addFooter(String text) {
        return add(new Footer(text));
    }

    public Entry addFooter(int stringResource) {
        return add(new Footer(stringResource));
    }

    protected Entry getFooter() {
        Entry last = !entries.isEmpty() ? entries.get(entries.size() - 1) : null;
        return last != null && last.getType() == Type.FOOTER ? last : null;
    }

    public void remove(Entry entry) {
        entries.remove(entry);
        onInvalidated();
    }

    public <T extends Entry> T add(T entry) {
        // header always as first element
        if (entry.getType() == Type.HEADER && !entries.isEmpty()) {
            throw new IllegalArgumentException("You can add header only as first element");
        }

        // footer only once and elements are not allowed after footer inserted
        Entry footer = getFooter();
        if (footer != null) {
            throw new IllegalArgumentException("You can't add more elements because footer already inserted.");
        }

        entries.add(entry);
        onInvalidated();
        return entry;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(Entry item) {
        Type type = item.getType();
        return type == Type.TEXT || type == Type.HEADER;
    }

    @Override
    public View getView(Entry entry, View view, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(entry.getType().resource, parent, false);
        }

        switch (entry.getType()) {
            case TEXT:
                Text text = (Text) entry;
                TextView textView = (TextView) view.findViewById(R.id.menu_text_text);
                ImageView imageView = (ImageView) view.findViewById(R.id.menu_text_image);
                setText(textView, text);
                setImage(imageView, text);
                break;
            case HEADER:
                Text header = (Text) entry;
                TextView headerStringView = (TextView) view.findViewById(R.id.menu_header_text);
                ImageView headerImageView = (ImageView) view.findViewById(R.id.menu_header_image);
                setText(headerStringView, header);
                setImage(headerImageView, header);
                break;
            case FOOTER:
                Footer footer = (Footer) entry;
                TextView footerTextView = (TextView) view;
                setText(footerTextView, footer);
                break;
        }
        return view;
    }

    private void setText(TextView view, AbstractStringEntry entry) {
        view.setText(entry.getString());
        if (entry.getStringResource() > 0) {
            view.setText(entry.getStringResource());
        }
    }

    private void setImage(ImageView view, Text entry) {
        if (entry.getImageResource() != null) {
            view.setImageResource(entry.getImageResource());
        } else if (entry.getImageBitmap() != null) {
            view.setImageBitmap(entry.getImageBitmap());
        } else if (entry.getImageDrawable() != null) {
            view.setImageDrawable(entry.getImageDrawable());
        }
    }

    @Override
    public Object getItemViewType(Entry item) {
        return item.getType();
    }

    @Override
    public int getViewTypeCount() {
        return Type.values().length;
    }

    @Override
    public List<Entry> getList() {
        return entries;
    }

    public enum Type {
        HEADER(R.layout.menu_header),
        TEXT(R.layout.menu_text),
        SEPARATOR(R.layout.menu_separator),
        FOOTER(R.layout.menu_footer);

        private int resource;

        Type(int resource) {
            this.resource = resource;
        }
    }

    public static abstract class Entry {
        public abstract Type getType();

        private Entry() {
        }
    }

    public static class Separator extends Entry {
        @Override
        public Type getType() {
            return Type.SEPARATOR;
        }
    }

    public class AbstractStringEntry extends Entry {
        private String string;
        private int stringResource;

        private AbstractStringEntry(String string) {
            setString(string);
        }

        private AbstractStringEntry(int stringResource) {
            setStringResource(stringResource);
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
            this.stringResource = 0;
            onChanged();
        }

        public int getStringResource() {
            return stringResource;
        }

        public void setStringResource(int stringResource) {
            this.stringResource = stringResource;
            this.string = null;
            onChanged();
        }

        @Override
        public Type getType() {
            return Type.TEXT;
        }
    }

    public class Text extends AbstractStringEntry {
        private Integer imageResource;
        private Bitmap imageBitmap;
        private Drawable imageDrawable;

        Text(String string) {
            super(string);
        }

        Text(int stringResource) {
            super(stringResource);
        }

        Text(String string, int imageResource) {
            this(string);
            setImageResource(imageResource);
        }

        Text(String string, Bitmap imageBitmap) {
            this(string);
            setImageBitmap(imageBitmap);
        }

        Text(int stringResource, int imageResource) {
            this(stringResource);
            setImageResource(imageResource);
        }

        Text(int stringResource, Bitmap imageBitmap) {
            this(stringResource);
            setImageBitmap(imageBitmap);
        }

        Text(int stringResource, Drawable imageDrawable) {
            this(stringResource);
            setImageDrawable(imageDrawable);
        }

        Text(String string, Drawable imageDrawable) {
            this(string);
            setImageDrawable(imageDrawable);
        }

        private void clearImages() {
            imageBitmap = null;
            imageResource = null;
            imageDrawable = null;
        }

        public Integer getImageResource() {
            return imageResource;
        }

        public void setImageResource(Integer imageResource) {
            clearImages();
            this.imageResource = imageResource;
            onChanged();
        }

        public Bitmap getImageBitmap() {
            return imageBitmap;
        }

        public void setImageBitmap(Bitmap imageBitmap) {
            clearImages();
            this.imageBitmap = imageBitmap;
            onChanged();
        }

        public Drawable getImageDrawable() {
            return imageDrawable;
        }

        public void setImageDrawable(Drawable imageDrawable) {
            clearImages();
            this.imageDrawable = imageDrawable;
        }
    }

    public class Footer extends AbstractStringEntry {

        Footer(String string) {
            super(string);
        }

        Footer(int stringResource) {
            super(stringResource);
        }

        @Override
        public Type getType() {
            return Type.FOOTER;
        }
    }

    public class Header extends Text {

        Header(String string) {
            super(string);
        }

        Header(int stringResource) {
            super(stringResource);
        }

        Header(String string, int imageResource) {
            super(string, imageResource);
        }

        Header(String string, Bitmap imageBitmap) {
            super(string, imageBitmap);
        }

        Header(int string, int imageResource) {
            super(string, imageResource);
        }

        Header(int string, Bitmap imageBitmap) {
            super(string, imageBitmap);
        }

        Header(String string, Drawable imageDrawable) {
            super(string, imageDrawable);
        }

        @Override
        public Type getType() {
            return Type.HEADER;
        }
    }

    public interface OnEntryClickListener {
        void onEntryClick(ListView listView, Entry entry, int position, long id);
    }
}

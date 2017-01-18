/*
 * Copyright (C) 2015 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.databinding.testapp;

import android.databinding.ObservableArrayMap;
import android.databinding.ViewDataBinding;
import android.databinding.testapp.databinding.LayoutWithIncludeBinding;
import android.databinding.testapp.databinding.MergeContainingMergeBinding;
import android.databinding.testapp.databinding.MergeLayoutBinding;
import android.databinding.testapp.vo.NotBindableVo;

import android.test.UiThreadTest;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

public class IncludeTagTest extends BaseDataBinderTest<LayoutWithIncludeBinding> {

    public IncludeTagTest() {
        super(LayoutWithIncludeBinding.class);
    }

    @UiThreadTest
    public void testIncludeTag() {
        initBinder();
        assertNotNull(mBinder.includedPlainLayout);
        assertTrue(mBinder.includedPlainLayout instanceof FrameLayout);
        NotBindableVo vo = new NotBindableVo(3, "a");
        mBinder.setOuterObject(vo);
        mBinder.executePendingBindings();
        final TextView outerText = (TextView) mBinder.getRoot().findViewById(R.id.outerTextView);
        assertEquals("a", outerText.getText());
        final TextView innerText = (TextView) mBinder.getRoot().findViewById(R.id.innerTextView);
        assertEquals("modified 3a", innerText.getText().toString());
        TextView textView1 = (TextView) mBinder.getRoot().findViewById(R.id.innerTextView1);
        assertEquals(mBinder.getRoot(), textView1.getParent().getParent());
        TextView textView2 = (TextView) mBinder.getRoot().findViewById(R.id.innerTextView2);
        assertEquals(mBinder.getRoot(), textView2.getParent().getParent());
        assertEquals("a hello 3a", textView1.getText().toString());
        assertEquals("b hello 3a", textView2.getText().toString());
        MergeLayoutBinding mergeLayoutBinding = mBinder.secondMerge;
        assertNotSame(textView1, mergeLayoutBinding.innerTextView1);
        assertNotSame(textView2, mergeLayoutBinding.innerTextView2);
        assertEquals("a goodbye 3a", mergeLayoutBinding.innerTextView1.getText().toString());
        assertEquals("b goodbye 3a", mergeLayoutBinding.innerTextView2.getText().toString());
        MergeContainingMergeBinding mergeContainingMergeBinding = mBinder.thirdMerge;
        MergeLayoutBinding merge1 = mergeContainingMergeBinding.merge1;
        MergeLayoutBinding merge2 = mergeContainingMergeBinding.merge2;
        assertEquals("a 1 third 3a", merge1.innerTextView1.getText().toString());
        assertEquals("b 1 third 3a", merge1.innerTextView2.getText().toString());
        assertEquals("a 2 third 3a", merge2.innerTextView1.getText().toString());
        assertEquals("b 2 third 3a", merge2.innerTextView2.getText().toString());

        vo.setIntValue(5);
        vo.setStringValue("b");
        mBinder.invalidateAll();
        mBinder.executePendingBindings();
        assertEquals("b", outerText.getText());
        assertEquals("modified 5b", innerText.getText().toString());
        assertEquals("a hello 5b", textView1.getText().toString());
        assertEquals("b hello 5b", textView2.getText().toString());
        assertEquals("a goodbye 5b", mergeLayoutBinding.innerTextView1.getText().toString());
        assertEquals("b goodbye 5b", mergeLayoutBinding.innerTextView2.getText().toString());
        assertEquals("a 1 third 5b", merge1.innerTextView1.getText().toString());
        assertEquals("b 1 third 5b", merge1.innerTextView2.getText().toString());
        assertEquals("a 2 third 5b", merge2.innerTextView1.getText().toString());
        assertEquals("b 2 third 5b", merge2.innerTextView2.getText().toString());

        assertEquals(View.VISIBLE, mBinder.includedPlainLayout.getVisibility());
        assertEquals(View.VISIBLE, mBinder.includedLayout.getRoot().getVisibility());
        mBinder.setVisibility(View.INVISIBLE);
        mBinder.executePendingBindings();
        assertEquals(View.INVISIBLE, mBinder.includedPlainLayout.getVisibility());
        assertEquals(View.INVISIBLE, mBinder.includedLayout.getRoot().getVisibility());
    }

    // Make sure that when an included layout's executePendingBindings is run that the includer
    // is run prior.
    @UiThreadTest
    public void testCorrectOrder() throws Throwable {
        initBinder();
        NotBindableVo.sTrackedValues.clear();
        NotBindableVo vo = new NotBindableVo(3, "a");
        mBinder.setOuterObject(vo);
        mBinder.trackedInclude.executePendingBindings();

        // Ensure that the including binding is evaluated before the included binding
        assertNotNull(mBinder.trackedInclude.getInnerObject());
        assertFalse(NotBindableVo.sTrackedValues.isEmpty());
        assertNotNull(NotBindableVo.sTrackedValues.get(0));
        assertEquals(1, NotBindableVo.sTrackedValues.size());
    }

    // Make sure that includes don't cause infinite loops with requestRebind
    public void testNoInfiniteLoop() throws Throwable {
        initBinder();
        NotBindableVo vo = new NotBindableVo(3, "a");
        mBinder.setOuterObject(vo);

        waitForUISync();

        // Make sure that a rebind hasn't been requested again after executePendingBindings
        Field field = ViewDataBinding.class.getDeclaredField("mPendingRebind");
        field.setAccessible(true);
        assertFalse(field.getBoolean(mBinder));
        assertFalse(field.getBoolean(mBinder.trackedInclude));
        assertFalse(field.getBoolean(mBinder.includedLayout));
    }

    // Make sure that including with a generic parameter works
    public void testGenericIncludeValue() throws Throwable {
        initBinder();
        NotBindableVo vo = new NotBindableVo();
        ObservableArrayMap<String, String> map = new ObservableArrayMap<>();
        map.put("value", "Hello World");
        mBinder.setOuterObject(vo);
        mBinder.setMap(map);

        waitForUISync();

        assertEquals("Hello World",
                mBinder.includedLayout.innerViewWithMapItem.getText().toString());
    }
}

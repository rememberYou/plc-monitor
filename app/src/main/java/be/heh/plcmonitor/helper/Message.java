/*
 * Copyright 2017 Terencio Agozzino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.heh.plcmonitor.helper;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

/**
 * Message allows you to display a center message at the bottom of the screen.
 *
 * @author Terencio Agozzino
 */
public class Message {

    /**
     * Display a center message with a Snackbar.
     *
     * @param mCoordinatorLayoutView the view to find a parent from
     * @param message the message to display
     * @param duration how long to display the message
     */
    public static void display(@NonNull View mCoordinatorLayoutView,
                               @NonNull CharSequence message, int duration) {
        Snackbar mSnackbar = Snackbar.make(mCoordinatorLayoutView, message,
                duration);

        View mView = mSnackbar.getView();
        TextView mTextView = mView.findViewById(android.support.design.R.id.snackbar_text);
        mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        mSnackbar.show();
    }
}
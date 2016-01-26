package com.taserlag.lasertag.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import com.parse.ParseException;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.game.FFAGame;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateGameFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateGameFragment extends Fragment {

    private Button[] gameTypeButtons = new Button[3];

    private OnFragmentInteractionListener mListener;

    public CreateGameFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateGameFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateGameFragment newInstance(String param1, String param2) {
        return new CreateGameFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_game, container, false);
        gameTypeButtons[0] = (Button) view.findViewById(R.id.button_tdm);
        gameTypeButtons[1] = (Button) view.findViewById(R.id.button_ffa);
        gameTypeButtons[2] = (Button) view.findViewById(R.id.button_vip);
        TouchListener tl = new TouchListener();

        gameTypeButtons[0].setOnTouchListener(tl);
        gameTypeButtons[1].setOnTouchListener(tl);
        gameTypeButtons[2].setOnTouchListener(tl);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    private class TouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            for(Button b:gameTypeButtons){
                b.setPressed(false);
            }
            v.setPressed(true);
            return true;
        }
    }

    public void saveGame() {
        FFAGame game = new FFAGame();
        game.setScoreEnabled(((Switch) getView().findViewById(R.id.switch_score)).isChecked());
        game.setScore(Integer.parseInt(((Spinner) getView().findViewById(R.id.spinner_score)).getSelectedItem().toString()));
        game.setTimeEnabled(((Switch) getView().findViewById(R.id.switch_time)).isChecked());
        game.setMinutes(Integer.parseInt(((Spinner) getView().findViewById(R.id.spinner_time)).getSelectedItem().toString()));
        game.setFriendlyFire(((Switch) getView().findViewById(R.id.switch_friendly_fire)).isChecked());
        game.setPrivateMatch(((Switch) getView().findViewById(R.id.switch_private)).isChecked());
        try {
            game.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

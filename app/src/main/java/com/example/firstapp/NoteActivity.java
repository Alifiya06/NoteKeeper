package com.example.firstapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final String Note_Position ="com.example.firstapp.Note_Position";
    public static final int Position_not_set = -1;
    public static final int POSITION_NOT_SET = Position_not_set;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinner_types;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;
    private NoteActivityViewModel mViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
        ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if(mViewModel.mIsNewlyCreated &&  savedInstanceState != null)
            mViewModel.restoreState(savedInstanceState);

        mViewModel.mIsNewlyCreated = false;


        mSpinner_types = findViewById(R.id.spinner_types);
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        Context context=this;
        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>( context = this,android.R.layout.simple_spinner_item,courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner_types.setAdapter(adapterCourses);

        readDisplayStateValues();
        saveOriginalNoteValues();

        mTextNoteTitle = findViewById(R.id.tittle_text);
        mTextNoteText = findViewById(R.id.note_text);
        if(!mIsNewNote)
        displayNote(mSpinner_types, mTextNoteTitle, mTextNoteText);

    }

    private void saveOriginalNoteValues() {

        if(mIsNewNote)
            return;

        mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalNoteTitle = mNote.getTitle();
        mViewModel.mOriginalNoteText = mNote.getText();
    }

    private void displayNote(Spinner spinner_types, EditText textNoteTitle, EditText textNoteText) {

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        spinner_types.setSelection(courseIndex);

        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());
    }

    private void readDisplayStateValues() {
        Intent intent=getIntent();
        int position = intent.getIntExtra(Note_Position, POSITION_NOT_SET);
        mIsNewNote = position==Position_not_set;
        if(mIsNewNote){
            createNewNote();
        } else{
            mNote=DataManager.getInstance().getNotes().get(position);
        }

    }

    private void createNewNote() {
        DataManager dm= DataManager.getInstance();
        mNotePosition = dm.createNewNote();
        mNote=dm.getNotes().get(mNotePosition);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if(id==R.id.action_cancel){
            mIsCancelling = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling){
            if(mIsNewNote)
                DataManager.getInstance().removeNote(mNotePosition);
            else {
                storePreviousNoteValues();
            }
        } else{
            saveNote();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState != null)
            mViewModel.saveState(outState);
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mViewModel.mOriginalNoteTitle);
        mNote.setText(mViewModel.mOriginalNoteText);

    }

    private void saveNote() {
        mNote.setCourse((CourseInfo)mSpinner_types.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }

    private void sendEmail() {
        CourseInfo course=(CourseInfo)mSpinner_types.getSelectedItem();
        String subject=mTextNoteTitle.getText().toString();
        String text="My new note \""+course.getTitle()+"\"\n"+mTextNoteText.getText();
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");     //Standard MIME for emails
        intent.putExtra(Intent.EXTRA_SUBJECT,subject);
        intent.putExtra(Intent.EXTRA_TEXT,text);
        startActivity(intent);
    }
}

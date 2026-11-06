package com.qunfeer.minesave.Controller;

import org.eclipse.jgit.revwalk.RevCommit;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GitView {
    private RevCommit rc;

    public RevCommit getRc() {
        return rc;
    }

    public void setRc(RevCommit rc) {
        this.rc = rc;
    }

    public GitView(RevCommit rc) {
        this.rc = rc;
    }
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        return sdf.format(new Date(rc.getCommitTime() * 1000L)) + "\t" + rc.getFullMessage();
    }
}

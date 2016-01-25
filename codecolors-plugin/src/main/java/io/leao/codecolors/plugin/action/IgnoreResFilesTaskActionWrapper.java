package io.leao.codecolors.plugin.action;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.TaskExecutionHistory;
import org.gradle.api.internal.changedetection.TaskArtifactState;
import org.gradle.api.internal.file.collections.SimpleFileCollection;
import org.gradle.api.internal.tasks.ContextAwareTaskAction;
import org.gradle.api.internal.tasks.TaskExecutionContext;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import io.leao.codecolors.plugin.file.FileUtils;
import io.leao.codecolors.plugin.file.IgnoreResFileFile;

public class IgnoreResFilesTaskActionWrapper implements ContextAwareTaskAction {

    private ContextAwareTaskAction mTask;
    private Set<File> mResFiles;

    public IgnoreResFilesTaskActionWrapper(ContextAwareTaskAction task, Set<File> resFiles) {
        mTask = task;
        mResFiles = resFiles;
    }

    @Override
    public void contextualise(TaskExecutionContext taskExecutionContext) {
        mTask.contextualise(
                taskExecutionContext != null ?
                        new IgnoreResFilesTaskExecutionContextWrapper(taskExecutionContext, mResFiles) : null);
    }

    @Override
    public void execute(Task task) {
        if (mTask != null) {
            mTask.execute(task);
        }
    }

    private static class IgnoreResFilesTaskExecutionContextWrapper implements TaskExecutionContext {
        private Set<File> mResFiles;

        private TaskArtifactState mTaskArtifactState;

        public IgnoreResFilesTaskExecutionContextWrapper(TaskExecutionContext taskExecutionContext,
                                                         Set<File> resFiles) {
            mResFiles = resFiles;
            setTaskArtifactState(taskExecutionContext.getTaskArtifactState());
        }

        @Override
        public TaskArtifactState getTaskArtifactState() {
            return mTaskArtifactState;
        }

        @Override
        public void setTaskArtifactState(TaskArtifactState taskArtifactState) {
            if (taskArtifactState != null) {
                mTaskArtifactState = new IgnoreResFilesTaskArtifactStateWrapper(taskArtifactState, mResFiles);
            } else {
                mTaskArtifactState = null;
            }
        }
    }

    private static class IgnoreResFilesTaskArtifactStateWrapper implements TaskArtifactState {
        private TaskArtifactState mTaskArtifactState;
        private IncrementalTaskInputs mIncrementalTaskInputs;
        private TaskExecutionHistory mTaskExecutionHistory;

        public IgnoreResFilesTaskArtifactStateWrapper(TaskArtifactState taskArtifactState, Set<File> resFiles) {
            mTaskArtifactState = taskArtifactState;
            mIncrementalTaskInputs =
                    new IgnoreResFilesIncrementalTaskInputsWrapper(taskArtifactState.getInputChanges(), resFiles);
            mTaskExecutionHistory =
                    new IgnoreResFilesTaskExecutionHistoryWrapper(taskArtifactState.getExecutionHistory(), resFiles);
        }

        @Override
        public boolean isUpToDate(Collection<String> collection) {
            return mTaskArtifactState.isUpToDate(collection);
        }

        @Override
        public IncrementalTaskInputs getInputChanges() {
            return mIncrementalTaskInputs;
        }

        @Override
        public void beforeTask() {
            mTaskArtifactState.beforeTask();
        }

        @Override
        public void afterTask() {
            mTaskArtifactState.afterTask();
        }

        @Override
        public void finished() {
            mTaskArtifactState.finished();
        }

        @Override
        public TaskExecutionHistory getExecutionHistory() {
            return mTaskExecutionHistory;
        }
    }

    private static class IgnoreResFilesTaskExecutionHistoryWrapper implements TaskExecutionHistory {
        private TaskExecutionHistory mTaskExecutionHistory;
        private Set<File> mResFiles;

        public IgnoreResFilesTaskExecutionHistoryWrapper(TaskExecutionHistory taskExecutionHistory,
                                                         Set<File> resFiles) {
            mTaskExecutionHistory = taskExecutionHistory;
            mResFiles = resFiles;
        }

        @Override
        public FileCollection getOutputFiles() {
            ArrayList<File> newOuputFiles = new ArrayList<>();
            for (File file : mTaskExecutionHistory.getOutputFiles()) {
                newOuputFiles.add(
                        FileUtils.inFolder(file, mResFiles) ?
                                new IgnoreResFileFile(file.getPath(), mResFiles) : file);
            }
            return new SimpleFileCollection(newOuputFiles);
        }
    }

    private static class IgnoreResFilesIncrementalTaskInputsWrapper implements IncrementalTaskInputs {
        private IncrementalTaskInputs mIncrementalTaskInputs;
        private Set<File> mResFiles;

        public IgnoreResFilesIncrementalTaskInputsWrapper(IncrementalTaskInputs incrementalTaskInputs,
                                                          Set<File> resFiles) {
            mIncrementalTaskInputs = incrementalTaskInputs;
            mResFiles = resFiles;
        }

        @Override
        public boolean isIncremental() {
            return mIncrementalTaskInputs.isIncremental();
        }

        @Override
        public void outOfDate(Action<? super InputFileDetails> action) {
            mIncrementalTaskInputs.outOfDate(new IgnoreResFilesActionWrapper(action, mResFiles));
        }

        @Override
        public void removed(Action<? super InputFileDetails> action) {
            mIncrementalTaskInputs.removed(new IgnoreResFilesActionWrapper(action, mResFiles));
        }
    }

    private static class IgnoreResFilesActionWrapper implements Action<InputFileDetails> {
        private Action<? super InputFileDetails> mAction;
        private Set<File> mResFiles;

        public IgnoreResFilesActionWrapper(Action<? super InputFileDetails> action, Set<File> resFiles) {
            mAction = action;
            mResFiles = resFiles;
        }

        @Override
        public void execute(InputFileDetails inputFileDetails) {
            if (!mResFiles.contains(inputFileDetails.getFile())) {
                mAction.execute(new IgnoreResFilesInputFileDetailsWrapper(inputFileDetails, mResFiles));
            }
        }
    }

    private static class IgnoreResFilesInputFileDetailsWrapper implements InputFileDetails {
        private InputFileDetails mInputFileDetails;
        private IgnoreResFileFile mFile;

        public IgnoreResFilesInputFileDetailsWrapper(InputFileDetails inputFileDetails, Set<File> resFiles) {
            mInputFileDetails = inputFileDetails;
            mFile = new IgnoreResFileFile(inputFileDetails.getFile().getPath(), resFiles);
        }

        @Override
        public boolean isAdded() {
            return mInputFileDetails.isAdded();
        }

        @Override
        public boolean isModified() {
            return mInputFileDetails.isModified();
        }

        @Override
        public boolean isRemoved() {
            return mInputFileDetails.isRemoved();
        }

        @Override
        public File getFile() {
            return mFile;
        }
    }
}
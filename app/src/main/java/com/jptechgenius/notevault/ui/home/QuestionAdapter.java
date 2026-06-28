package com.jptechgenius.notevault.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.jptechgenius.notevault.data.remote.model.QuestionResponse;
import com.jptechgenius.notevault.databinding.ItemQuestionCardBinding;
import java.util.ArrayList;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private List<QuestionResponse> questions = new ArrayList<>();
    private final QuestionClickListener listener;

    public interface QuestionClickListener {
        void onQuestionClick(QuestionResponse question);
    }

    public QuestionAdapter(QuestionClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<QuestionResponse> newList) {
        this.questions = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQuestionCardBinding binding = ItemQuestionCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new QuestionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        holder.bind(questions.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        private final ItemQuestionCardBinding binding;

        QuestionViewHolder(ItemQuestionCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(QuestionResponse q, QuestionClickListener listener) {
            binding.tvQuestionTitle.setText(q.getCourseName());
            binding.tvExamTypeBadge.setText(q.getExamType().toUpperCase());
            binding.tvDeptInfo.setText("Dept of " + q.getDepartmentShort());
            binding.tvSemesterBadge.setText(q.getSemesterName());
            binding.tvSubmissions.setText(q.getSubmissionsCount() + " Submissions");
            
            binding.btnViewQuestion.setOnClickListener(v -> listener.onQuestionClick(q));
            itemView.setOnClickListener(v -> listener.onQuestionClick(q));
        }
    }
}

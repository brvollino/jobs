package com.vollino.poll.service.vote;

import com.vollino.poll.service.exception.DataIntegrityException;
import com.vollino.poll.service.poll.Clock;
import com.vollino.poll.service.poll.Poll;
import com.vollino.poll.service.poll.PollRepository;
import com.vollino.poll.service.vote.exception.ClosedPollException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author Bruno Vollino
 */
@Service
@Validated
public class VoteService {

    private final VoteRepository voteRepository;
    private final PollRepository pollRepository;
    private final Clock clock;

    @Autowired
    public VoteService(VoteRepository voteRepository, PollRepository pollRepository, Clock clock) {
        this.voteRepository = voteRepository;
        this.pollRepository = pollRepository;
        this.clock = clock;
    }

    public Vote create(@Valid Vote vote) {
        Optional<Poll> poll = pollRepository.findById(vote.getId().getPollId());
        if (poll.isPresent()) {
            if (clock.now().isAfter(poll.get().getEndDate())) {
                throw new ClosedPollException(poll.get());
            }
        } else {
            throw new DataIntegrityException(String.format("Poll with id=%d not found", vote.getId().getPollId()));
        }
        if (voteRepository.existsById(vote.getId())) {
            throw new DataIntegrityException(String.format(
                    "Voter with id=%d has already voted in Poll %d",
                    vote.getId().getPollId(), vote.getId().getVoterId()));
        }

        return voteRepository.save(vote);
    }
}

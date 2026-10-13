-- A handful of answered questions marked as FAQ, so the public FAQ page has
-- something to show before any customer has asked anything and the shop's
-- answering story (Epic 5) exists.
--
-- The asker is the seeded demo customer and the answerer the seeded demo client;
-- both rows come from V1_3, so this migration assumes the demo accounts exist.
-- Using the seeded accounts rather than a dedicated author keeps the foreign keys
-- to app_user real while adding no new identity to clean up.
--
-- is_faq is TRUE and answered_at is set here, which is what the public FAQ read
-- filters on (see QuestionRepository.findAnsweredFaqs): an unanswered or
-- unmarked row would be invisible, so seeding one that way would seed nothing.

INSERT INTO question (customer_id, question_text, answer_text, is_faq, answered_at, answered_by)
VALUES
    ((SELECT id FROM app_user WHERE email = 'customer@myopty.local'),
     'How long does delivery take?',
     'Most orders arrive within 3-5 working days. Orders with custom-made lenses can take up to 7 working days.',
     TRUE, CURRENT_TIMESTAMP(6),
     (SELECT id FROM app_user WHERE email = 'client@myopty.local')),
    ((SELECT id FROM app_user WHERE email = 'customer@myopty.local'),
     'Can I use my own prescription?',
     'Yes. Bring or upload a valid prescription from your optometrist and we will make the lenses to it.',
     TRUE, CURRENT_TIMESTAMP(6),
     (SELECT id FROM app_user WHERE email = 'client@myopty.local')),
    ((SELECT id FROM app_user WHERE email = 'customer@myopty.local'),
     'What is your return policy?',
     'Unused frames can be returned within 14 days of delivery. Custom-cut lenses cannot be returned once fitted.',
     TRUE, CURRENT_TIMESTAMP(6),
     (SELECT id FROM app_user WHERE email = 'client@myopty.local'));

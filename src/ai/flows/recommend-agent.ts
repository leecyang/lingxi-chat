'use server';

/**
 * @fileOverview This file defines a Genkit flow for recommending an appropriate intelligent agent based on user input.
 *
 * - recommendAgent - A function that takes user input and returns a recommended agent.
 * - RecommendAgentInput - The input type for the recommendAgent function.
 * - RecommendAgentOutput - The return type for the recommendAgent function.
 */

import {ai} from '@/ai/genkit';
import {z} from 'genkit';

const RecommendAgentInputSchema = z.object({
  userInput: z.string().describe('The user input or query.'),
  availableAgents: z.array(
    z.object({
      name: z.string().describe('The name of the intelligent agent.'),
      description: z.string().describe('A description of the agent\'s capabilities.'),
    })
  ).describe('A list of available intelligent agents and their descriptions.'),
});
export type RecommendAgentInput = z.infer<typeof RecommendAgentInputSchema>;

const RecommendAgentOutputSchema = z.object({
  recommendedAgent: z.string().describe('The name of the recommended intelligent agent.'),
  reason: z.string().describe('The reason for recommending this agent.'),
});
export type RecommendAgentOutput = z.infer<typeof RecommendAgentOutputSchema>;

export async function recommendAgent(input: RecommendAgentInput): Promise<RecommendAgentOutput> {
  return recommendAgentFlow(input);
}

const prompt = ai.definePrompt({
  name: 'recommendAgentPrompt',
  input: {schema: RecommendAgentInputSchema},
  output: {schema: RecommendAgentOutputSchema},
  prompt: `Given the following user input:

  {{userInput}}

  And the following available intelligent agents:

  {{#each availableAgents}}
  - Name: {{this.name}}
    Description: {{this.description}}
  {{/each}}

  Recommend the most suitable agent to handle the user input. Explain your reasoning.
  Ensure that the agent recommended is in the list of available agents.
`,
});

const recommendAgentFlow = ai.defineFlow(
  {
    name: 'recommendAgentFlow',
    inputSchema: RecommendAgentInputSchema,
    outputSchema: RecommendAgentOutputSchema,
  },
  async input => {
    const {output} = await prompt(input);
    return output!;
  }
);
